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
  private val updateInterval = 5 minutes

  private def events(numLast: Option[Int], lastUpdate: Instant): Future[List[Try[Status]]] = {
    val eventsFuture = GithubClient.events(org)
    numLast.
      map(n => eventsFuture.map(_.sortBy(_.date).takeRight(n))).
      getOrElse(eventsFuture).
      map(
        _.filter(_.date.isAfter(lastUpdate)).
          map(Try(_))
      ).
      recover { case e => List(Failure(e)) }
  }

  private def getLastUpdate(statuses: List[Try[Status]]): Option[Instant] =
    statuses.
      collect { case Success(s) => s }.
      lastOption.
      map(_.date)

  /**
    * Streams GitHub events.
    * @param numLast The number of previous events to prepend to the stream.
    * @return The source providing the stream.
    */
  override def source(numLast: Int): Source[Try[Status], _] =
    Source.
      unfoldAsync[(Boolean, Instant), List[Try[Status]]]((true, Instant.ofEpochSecond(0))) {
        case (first, lastUpdate) =>
          val (delay, numLastOption) =
            if (first) (0 seconds, Some(numLast))
            else (updateInterval, None)
          after(delay, using = system.scheduler) {
            events(numLastOption, lastUpdate).map { statuses =>
              Some(((false, getLastUpdate(statuses).getOrElse(lastUpdate)), statuses))
            }
          }
      }.
      flatMapConcat(list => Source.fromIterator(() => list.iterator))

}
