package ch.becompany.social.github

import ch.becompany.http.HttpClient
import ch.becompany.social.Status

import scala.concurrent.{ExecutionContext, Future}

object GithubClient extends HttpClient with GithubJsonSupport {

  val baseUrl = "https://api.github.com"

  def events(org: String)(implicit ec: ExecutionContext): Future[List[Status]] =
    req[List[Status]](s"$baseUrl/orgs/$org/events")

}
