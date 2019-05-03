package ch.becompany.social.facebook

import java.time.Instant

import ch.becompany.social.{PollingSocialFeed, Status}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration

class FacebookFeed(pageId: String, userUpdateInterval: FiniteDuration = 1 minute)(implicit ec: ExecutionContext)
  extends PollingSocialFeed
{

  private val updateIntervalMin: FiniteDuration = 1 minute
  override val updateInterval = updateIntervalMin.max(userUpdateInterval)

  /**
    * Returns the latest `num` social media status messages.
    *
    * @param num The number of previous status messages to prepend to the stream.
    * @return A list of status messages.
    */
  override def latest(num: Int): Future[List[(Instant, Status)]] = FacebookClient.posts(pageId)

}
