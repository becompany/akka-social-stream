package ch.becompany.social.github

import java.time.Instant

import akka.http.scaladsl.model.{HttpRequest, Uri}
import ch.becompany.http.{CachingSupport, HttpClient, UnmarshallingHttpHandler}
import ch.becompany.social.Status
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}

/**
  * Client for the GitHub REST API.
  */
object GithubClient extends HttpClient
  with CachingSupport
  with GithubJsonSupport
  with LazyLogging {

  private val baseUrl = "https://api.github.com"

  private implicit val handler = new UnmarshallingHttpHandler[Seq[(Instant, Status)]]()

  /**
    * Requests the latest events for an organization, ordered by date in ascending order.
    * @param org The organization name.
    * @param ec The execution context.
    * @return A future list of events.
    */
  def events(org: String)(implicit ec: ExecutionContext): Future[Seq[(Instant, Status)]] = {
    logger.debug(s"Requesting events for organization $org")
    req[Seq[(Instant, Status)]](HttpRequest(uri = Uri(s"$baseUrl/orgs/$org/events"))).recover { case _ => Seq.empty}
  }

}
