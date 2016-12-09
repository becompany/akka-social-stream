package ch.becompany.social.github

import akka.http.scaladsl.model.{HttpRequest, Uri}
import ch.becompany.http.{CachingHttpClient, HttpClient, UnmarshallingHttpHandler}
import ch.becompany.social.Status

import scala.concurrent.{ExecutionContext, Future}

object GithubClient extends CachingHttpClient with GithubJsonSupport {

  val baseUrl = "https://api.github.com"

  implicit val handler = new UnmarshallingHttpHandler[List[Status]]()

  def events(org: String)(implicit ec: ExecutionContext): Future[List[Status]] =
    req[List[Status]](HttpRequest(uri = Uri(s"$baseUrl/orgs/$org/events")))

}
