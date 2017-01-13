package ch.becompany.social.github

import akka.http.scaladsl.model.{HttpRequest, Uri}
import ch.becompany.http.{CachingSupport, HttpClient, UnmarshallingHttpHandler}
import ch.becompany.social.Status

import scala.concurrent.{ExecutionContext, Future}

/**
  * Client for the GitHub REST API.
  */
object GithubClient extends HttpClient with CachingSupport with GithubJsonSupport {

  private val baseUrl = "https://api.github.com"

  private implicit val handler = new UnmarshallingHttpHandler[List[Status]]()

  /**
    * Requests the latest events for an organization.
    * @param org The organization name.
    * @param ec The execution context.
    * @return A future list of events.
    */
  def events(org: String)(implicit ec: ExecutionContext): Future[List[Status]] =
    req[List[Status]](HttpRequest(uri = Uri(s"$baseUrl/orgs/$org/events")))

}
