package ch.becompany.social.twitter

import java.time.Instant

import akka.actor.ActorSystem
import akka.pattern.after
import akka.stream.scaladsl.Source
import ch.becompany.social.{SocialFeed, Status}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

/**
  * Streams Twitter tweets. The continuous stream is generated based on the
  * [[https://dev.twitter.com/streaming/reference/post/statuses/filter statuses/filter]] method
  * of the Twitter streaming API.
  * @param screenName The screen name of the user, see
  *   [[https://dev.twitter.com/overview/api/users Twitter Developer Documentation]].
  * @param ec The execution context.
  */
class TwitterFeed(screenName: String, userUpdateInterval: FiniteDuration = 1 minute)(implicit ec: ExecutionContext)
  extends SocialFeed {

  private val system = ActorSystem()
  private lazy val client = new TwitterClient(screenName)
  // Twitter unauthenticated requests are limited to 15 per hour. => 1 req/m
  private val updateIntervalMin: FiniteDuration = 1 minute
  private val updateInterval = updateIntervalMin.max(userUpdateInterval)

  /**
    * Returns the latest `num` Twitter tweets.
    * @param num The number of tweets to emit.
    * @return A future list of tweets.
    */
  override def latest(num: Int): Future[List[(Instant, Status)]] =
    client.latest(num)


  private def events(lastUpdate: Instant): Future[List[(Instant, Try[Status])]] =
    client.latest(5).
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
    * Streams Twitter tweets.
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
