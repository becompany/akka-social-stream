package ch.becompany.social.github

import java.time.Instant
import java.util.concurrent.TimeUnit

import akka.Done
import akka.actor.{ActorSystem, Cancellable}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Keep, Source}
import ch.becompany.social.{SocialFeed, Status}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.Duration
import scala.util.Try

class GithubEventsFeed(org: String)(implicit ec: ExecutionContext) extends SocialFeed {

  var lastUpdate: Option[Instant] = None

  val system = ActorSystem()
  val updateInterval = Duration.create(5, TimeUnit.MINUTES)

  override def source(numLast: Int): Source[Try[Status], _] = {
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
