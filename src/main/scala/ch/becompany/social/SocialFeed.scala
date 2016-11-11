package ch.becompany.social

import akka.stream.scaladsl.Source

import scala.util.Try

trait SocialFeed {

  def stream(numLast: Int): Source[Try[Status], _]

}
