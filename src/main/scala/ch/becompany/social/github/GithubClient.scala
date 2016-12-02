package ch.becompany.social.github

import ch.becompany.http.{CachingHttpClient, HttpClient}

import scala.concurrent.{ExecutionContext, Future}

object GithubClient extends CachingHttpClient with GithubJsonSupport {

  val baseUrl = "https://api.github.com"

  def events(org: String)(implicit ec: ExecutionContext): Future[List[Event]] =
    req[List[Event]](s"$baseUrl/orgs/$org/events")

  def repositories(org: String)(implicit ec: ExecutionContext): Future[List[Repository]] =
    req[List[Repository]](s"$baseUrl/orgs/$org/repos")

  def commits(owner: String, repo: String)(implicit ec: ExecutionContext): Future[List[CommitPage]] =
    req[List[CommitPage]](s"$baseUrl/repos/$owner/$repo/commits")

}
