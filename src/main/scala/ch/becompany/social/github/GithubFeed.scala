package ch.becompany.social.github

import java.time.Instant

import ch.becompany.social.{PollingSocialFeed, Status}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
  * Streams GitHub events for an organization.
  * @param org The name of the organization.
  * @param ec The execution context.
  */
class GithubFeed(org: String, userUpdateInterval: FiniteDuration = 1 minute)(implicit ec: ExecutionContext)
  extends PollingSocialFeed
{

  // GitHub unauthenticated requests are limited to 60 per hour. => 1 req/m
  private val updateIntervalMin: FiniteDuration = 1 minute
  override val updateInterval = updateIntervalMin.max(userUpdateInterval)

  /**
    * Returns the latest `num` GitHub events.
    * @param num The number of events to emit.
    * @return A future list of events.
    */
  override def latest(num: Int): Future[List[(Instant, Status)]] =
    GithubClient.events(org).map(_.sortBy(_._1).takeRight(num))
}
