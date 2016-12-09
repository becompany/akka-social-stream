package ch.becompany.social.twitter

import akka.stream.scaladsl.Source
import ch.becompany.social.{SocialFeed, Status}

import scala.concurrent.ExecutionContext
import scala.util.Try

class TwitterFeed(screenName: String)(implicit ec: ExecutionContext)
  extends SocialFeed {

  private lazy val client = new TwitterClient(screenName)
  private lazy val streamFuture = client.userId.map(id => TwitterStream("follow" -> id))

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
