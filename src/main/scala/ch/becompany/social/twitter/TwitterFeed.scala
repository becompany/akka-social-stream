package ch.becompany.social.twitter

import akka.stream.scaladsl.Source
import ch.becompany.social.{SocialFeed, Status}

import scala.concurrent.ExecutionContext
import scala.util.Try

class TwitterFeed(filter: Map[String, String])(implicit ec: ExecutionContext)
  extends SocialFeed {

  private lazy val stream = new TwitterStream(filter)

  override def source(numLast: Int): Source[Try[Status], _] =
    stream.stream
}

object TwitterFeed {
  def apply(filter: (String, String)*)(implicit ec: ExecutionContext): TwitterFeed =
    new TwitterFeed(filter.toMap)
}