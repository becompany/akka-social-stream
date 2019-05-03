package ch.becompany.social

import java.time.Instant

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.pattern.after

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Try}

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

/**
* Feed providing a stream of status messages from a social networking platform based on a polling implementation.
*/
trait PollingSocialFeed extends SocialFeed {

  private val numberOfEventsToFetch = 20

  val system = ActorSystem()
  val updateInterval: FiniteDuration

  /**
    * Create a stream that request 20 new events based on "updateInterval". The events are sorted by date.
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

  private def getLastUpdate(statuses: List[(Instant, Try[Status])]): Option[Instant] =
    statuses.
      lastOption.
      map(_._1)

  private def events(lastUpdate: Instant): Future[List[(Instant, Try[Status])]] =
    latest(numberOfEventsToFetch).
      map(_.
        filter(_._1.isAfter(lastUpdate)).
        sortBy(_._1).
        map { case (date, status) => (date, Try(status)) }
      ).
      recover { case e => List(Instant.now -> Failure(e)) }
}