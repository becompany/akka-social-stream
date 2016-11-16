package ch.becompany.social

import akka.stream.scaladsl.Source

import scala.util.Try

/**
  * Social feed.
  */
trait SocialFeed {

  def source(numLast: Int): Source[Try[Status], _]

}
