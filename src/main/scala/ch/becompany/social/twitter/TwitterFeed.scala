package ch.becompany.social.twitter

import akka.stream.scaladsl.Source
import ch.becompany.social.{SocialFeed, Status}

import scala.concurrent.ExecutionContext
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
    * Streams Twitter tweets.
    * @param numLast The number of previous tweets to prepend to the stream.
    * @return The source providing the stream.
    */
  override def source(numLast: Int): Source[Try[Status], _] = {
    val latestFuture = client.latest(numLast)
    val latestAndStream = for {
      latest <- latestFuture
      stream <- streamFuture
    } yield (latest, stream)
    Source.
      fromFuture(latestAndStream).
      flatMapConcat {
        case (latest, stream) =>
          Source.
            fromIterator(() => latest.map(Try(_)).iterator).
            concat(stream.stream)
      }
  }
}
