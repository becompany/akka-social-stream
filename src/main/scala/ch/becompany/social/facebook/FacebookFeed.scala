package ch.becompany.social.facebook

import java.time.Instant

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.pattern.after
import ch.becompany.social.{SocialFeed, Status}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Try}

class FacebookFeed(pageId: String, userUpdateInterval: FiniteDuration = 1 minute)(implicit ec: ExecutionContext) extends SocialFeed {

  private val system = ActorSystem()
  // GitHub unauthenticated requests are limited to 60 per hour. => 1 req/m
  private val updateIntervalMin: FiniteDuration = 1 minute
  private val updateInterval = updateIntervalMin.max(userUpdateInterval)
  /**
    * Returns the latest `num` social media status messages.
    *
    * @param num The number of previous status messages to prepend to the stream.
    * @return A list of status messages.
    */
  override def latest(num: Int): Future[List[(Instant, Status)]] = FacebookClient.posts(pageId)

  private def events(lastUpdate: Instant): Future[List[(Instant, Try[Status])]] =
    FacebookClient.posts(pageId).
      map(_.
        filter(_._1.isAfter(lastUpdate)).
        sortBy(_._1).
        map { case (date, status) => (date, Try(status)) }
      ).
      recover { case e => List(Instant.now -> Failure(e)) }

  private def getLastUpdate(statuses: List[(Instant, Try[Status])]): Option[Instant] =
    statuses.
      lastOption.
      map(_._1)

  /**
    * Streams future social media status messages.
    *
    * @return The source providing the stream.
    */
  override def stream(): Source[(Instant, Try[Status]), _] =
    Source.
      unfoldAsync[Instant, List[(Instant, Try[Status])]](Instant.now) { lastUpdate =>
      after(updateInterval, using = system.scheduler) {
        events(lastUpdate) map { statuses =>
          Some((getLastUpdate(statuses) getOrElse lastUpdate, statuses))
        }
      }
    }.
    flatMapConcat(list => Source.fromIterator(() => list.iterator))
}
