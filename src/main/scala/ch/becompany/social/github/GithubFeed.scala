package ch.becompany.social.github

import java.time.Instant

import akka.actor.ActorSystem
import akka.pattern.after
import akka.stream.scaladsl.Source
import ch.becompany.social.{SocialFeed, Status}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
  * Streams GitHub events for an organization.
  * @param org The name of the organization.
  * @param ec The execution context.
  */
class GithubFeed(org: String)(implicit ec: ExecutionContext) extends SocialFeed {

  private val system = ActorSystem()
  private val updateInterval = 1 minute

  /**
    * Returns the latest `num` GitHub events.
    * @param num The number of events to emit.
    * @return A future list of events.
    */
  override def latest(num: Int): Future[List[(Instant, Status)]] =
    GithubClient.events(org).map(_.sortBy(_._1).takeRight(num))

  private def events(lastUpdate: Instant): Future[List[(Instant, Try[Status])]] =
    GithubClient.events(org).
      map(_.
        filter(_._1.isAfter(lastUpdate)).
        sortBy(_._1).
        map { case (date, status) => (date, Try(status)) }
      ).
      recover { case e => List(Instant.now -> Failure(e)) }

  private def getLastUpdate(statuses: List[(Instant, Try[Status])]): Option[Instant] =
    statuses.
      lastOption.
      map(_._1)

  /**
    * Streams GitHub events. The stream is populated using periodic requests;
    * the interval between requests is set in the `updateInterval` value. The first
    * request is emitted after `updateInterval`.
    * @return The source providing the stream.
    */
  override def stream(): Source[(Instant, Try[Status]), _] =
    Source.
      unfoldAsync[Instant, List[(Instant, Try[Status])]](Instant.now) { lastUpdate =>
        after(updateInterval, using = system.scheduler) {
          events(lastUpdate) map { statuses =>
            Some((getLastUpdate(statuses) getOrElse lastUpdate, statuses))
          }
        }
      }.
      flatMapConcat(list => Source.fromIterator(() => list.iterator))

}
