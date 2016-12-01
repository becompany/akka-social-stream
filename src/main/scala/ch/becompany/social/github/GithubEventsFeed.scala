package ch.becompany.social.github

import java.time.Instant

import akka.actor.{ActorSystem, Cancellable}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Source
import ch.becompany.social.{SocialFeed, Status}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class GithubEventsFeed(org: String)(implicit ec: ExecutionContext) extends SocialFeed {

  private val system = ActorSystem()
  private val updateInterval = 5 minutes

  def events(numLast: Option[Int], lastUpdate: Instant): Future[List[Try[Status]]] = {
    val eventsFuture = GithubClient.events(org)
    numLast.
      map(n => eventsFuture.map(_.takeRight(n))).
      getOrElse(eventsFuture).
      map(
        _.filter(_.date.isAfter(lastUpdate)).
          sortBy(_.date).
          map(Try(_))
      ).
      recover { case e => List(Failure(e)) }
  }

  def getLastUpdate(statuses: List[Try[Status]]): Option[Instant] =
    statuses.
      collect { case Success(s) => s }.
      lastOption.
      map(_.date)

  override def source(numLast: Int): Source[Try[Status], _] =
    Source.
      unfoldAsync[(Option[Int], Instant), List[Try[Status]]]((Some(numLast), Instant.ofEpochSecond(0))) {
        case (numLastOption, lastUpdate) =>
          // TODO: Add delay
          events(numLastOption, lastUpdate).map { statuses =>
            Some(((None, getLastUpdate(statuses).getOrElse(lastUpdate)), statuses))
          }
      }.
      flatMapConcat(list => Source.fromIterator(() => list.iterator))

  /*
    eventSource(Some(numLast)).
      concat(
        Source.
          tick(0 seconds, updateInterval, ()).
          flatMapConcat(_ => eventSource())
      )
      */

/*
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
  */
}
