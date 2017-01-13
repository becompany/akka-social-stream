package ch.becompany.social

import akka.stream.scaladsl.Source

import scala.util.Try

/**
  * Feed providing a stream of status messages from a social networking platform.
  */
trait SocialFeed {

  /**
    * Streams social media status messages.
    * @param numLast The number of previous status messages to prepend to the stream.
    * @return The source providing the stream.
    */
  def source(numLast: Int): Source[Try[Status], _]

}
