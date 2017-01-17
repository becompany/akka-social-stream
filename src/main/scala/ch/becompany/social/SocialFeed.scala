package ch.becompany.social

import java.time.Instant

import akka.stream.scaladsl.Source

import scala.concurrent.Future
import scala.util.Try

/**
  * Feed providing a stream of status messages from a social networking platform.
  */
trait SocialFeed {

  /**
    * Returns the latest `num` social media status messages.
    * @param num The number of previous status messages to prepend to the stream.
    * @return A list of status messages.
    */
  def latest(num: Int): Future[List[(Instant, Status)]]

  /**
    * Streams future social media status messages.
    * @return The source providing the stream.
    */
  def stream(): Source[(Instant, Try[Status]), _]
}
