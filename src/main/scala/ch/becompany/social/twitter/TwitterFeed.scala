package ch.becompany.social.twitter

import akka.stream.scaladsl.Source
import ch.becompany.social.{SocialFeed, Status}

import scala.concurrent.ExecutionContext
import scala.util.Try

class TwitterFeed(user: String)(implicit ec: ExecutionContext)
  extends SocialFeed {

  private lazy val client = new TwitterClient(user)
  private lazy val stream = TwitterStream("follow" -> user)

  override def source(numLast: Int): Source[Try[Status], _] =
    Source.
      fromFuture(client.latest(numLast)).
      flatMapConcat(list => Source.fromIterator(() => list.map(Try(_)).iterator)).
      concat(stream.stream)
}
