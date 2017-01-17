package ch.becompany.social.twitter

import java.time.Instant

import akka.stream.scaladsl.Source
import ch.becompany.social.{SocialFeed, Status}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
  * Streams Twitter tweets. The continuous stream is generated based on the
  * [[https://dev.twitter.com/streaming/reference/post/statuses/filter statuses/filter]] method
  * of the Twitter streaming API.
  * @param screenName The screen name of the user, see
  *   [[https://dev.twitter.com/overview/api/users Twitter Developer Documentation]].
  * @param ec The execution context.
  */
class TwitterFeed(screenName: String)(implicit ec: ExecutionContext)
  extends SocialFeed {

  private lazy val client = new TwitterClient(screenName)
  private lazy val streamFuture = client.userId.map(id => TwitterStream("follow" -> id))

  /**
    * Returns the latest `num` Twitter tweets.
    * @param num The number of tweets to emit.
    * @return A future list of tweets.
    */
  override def latest(num: Int): Future[List[(Instant, Status)]] =
    client.latest(num)

  /**
    * Streams Twitter tweets.
    * @return The source providing the stream.
    */
  override def stream(): Source[(Instant, Try[Status]), _] =
    Source.
      fromFuture(streamFuture).
      flatMapConcat(_.stream)

}
