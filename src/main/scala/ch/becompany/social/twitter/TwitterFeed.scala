package ch.becompany.social.twitter

import akka.stream.scaladsl.Source
import ch.becompany.social.{SocialFeed, Status}

import scala.concurrent.ExecutionContext
import scala.util.Try

class TwitterFeed(screenName: String)(implicit ec: ExecutionContext)
  extends SocialFeed {

  private lazy val client = new TwitterClient(screenName)
  private lazy val stream = TwitterStream("follow" -> screenName)

  override def source(numLast: Int): Source[Try[Status], _] =
    Source.
      fromFuture(client.latest(numLast)).
      flatMapConcat(list => Source.fromIterator(() => list.map(Try(_)).iterator)).
      concat(stream.stream)
}
