package ch.becompany.social.twitter

import java.time.Instant

import ch.becompany.social.{PollingSocialFeed, Status}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Streams Twitter tweets. The continuous stream is generated based on polling with ETAG support.
  * @param screenName The screen name of the user, see
  *   [[https://dev.twitter.com/overview/api/users Twitter Developer Documentation]].
  * @param ec The execution context.
  */
class TwitterFeed(screenName: String, userUpdateInterval: FiniteDuration = 1 minute)(implicit ec: ExecutionContext)
  extends PollingSocialFeed {

  private lazy val client = new TwitterClient(screenName)
  // Twitter unauthenticated requests are limited to 15 per hour. => 1 req/m
  private val updateIntervalMin: FiniteDuration = 1 minute
  override val updateInterval = updateIntervalMin.max(userUpdateInterval)

  /**
    * Returns the latest `num` Twitter tweets.
    * @param num The number of tweets to emit.
    * @return A future list of tweets.
    */
  override def latest(num: Int): Future[Seq[(Instant, Status)]] =
    client.latest(num)

}
