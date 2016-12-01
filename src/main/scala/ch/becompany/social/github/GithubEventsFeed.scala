package ch.becompany.social.github

import java.time.Instant

import akka.actor.{ActorSystem, Cancellable}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Source
import ch.becompany.social.{SocialFeed, Status}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

class GithubEventsFeed(org: String)(implicit ec: ExecutionContext) extends SocialFeed {

  private var lastUpdate: Option[Instant] = None

  private val system = ActorSystem()
  private val updateInterval = 5 minutes

  def isNew(status: Status): Boolean =
    lastUpdate.forall(_.isBefore(status.date))

  def events(numLast: Option[Int]): Future[List[Try[Status]]] = {
    val eventsFuture = GithubClient.events(org)
    numLast.
      map(n => eventsFuture.map(_.takeRight(n))).
      getOrElse(eventsFuture).
      map(
        _.filter(isNew).
          sortBy(_.date).
          map { status =>
            lastUpdate = Some(status.date)
            Try(status)
          }
      ).
      recover { case e => List(Failure(e)) }
  }

  def eventSource(numLast: Option[Int] = None): Source[Try[Status], _] =
    Source.
      fromFuture(events(numLast)).
      flatMapConcat(list => Source.fromIterator(() => list.iterator))

  override def source(numLast: Int): Source[Try[Status], _] =
    eventSource(Some(numLast)).
      concat(
        Source.
          tick(0 seconds, updateInterval, ()).
          flatMapConcat(_ => eventSource())
      )


  def source_DISABLED(numLast: Int): Source[Try[Status], _] = {
    var cancellable: Cancellable = null
    Source.queue[Try[Status]](bufferSize = 1000, OverflowStrategy.dropTail)
        .mapMaterializedValue { queue =>
          // TODO: cancel the Github client on stream termination.
          cancellable = system.scheduler.schedule(Duration.Zero, updateInterval) {
            GithubClient.events(org).foreach(_.sortBy(_.date).takeRight(numLast).foreach(status => {
              lastUpdate match {
                case Some(lastDate: Instant) => {
                  if (lastDate.isBefore(status.date)) {
                    lastUpdate = Some(status.date)
                    queue.offer(Try(status))
                  }
                }
                case None => {
                  lastUpdate = Some(status.date)
                  queue.offer(Try(status))
                }
              }
            }))
          }
      }
  }
}
