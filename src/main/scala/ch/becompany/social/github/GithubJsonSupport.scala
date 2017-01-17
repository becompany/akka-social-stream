package ch.becompany.social.github

import java.time.Instant

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import ch.becompany.json._
import ch.becompany.social.{Status, User}
import spray.json._

trait ReadOnlyJsonFormat[T] extends JsonFormat[T] {
  def write(e: T) = throw new SerializationException("not supported")
}

trait GithubJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  import JsonExtractor._

  val extractUser = EJsObject("login" -> EJsString)
  val extractRepo = EJsObject("name" -> EJsString)

  private def repoUrl(repo: String) = s"https://github.com/$repo"

  object PushEvent extends JsonExtractor(
    EJsObject(
      "type" -> JsString("PushEvent"),
      "created_at" -> EJsString,
      "actor" -> extractUser,
      "payload" -> EJsObject(
        "size" -> EJsNumber
      ),
      "repo" -> extractRepo
    )
  )

  object CreateRepositoryEvent extends JsonExtractor(
    EJsObject(
      "type" -> JsString("CreateEvent"),
      "created_at" -> EJsString,
      "actor" -> extractUser,
      "payload" -> EJsObject(
        "ref_type" -> JsString("repository")
      ),
      "repo" -> extractRepo
    )
  )

  object CreateBranchEvent extends JsonExtractor(
    EJsObject(
      "type" -> JsString("CreateEvent"),
      "created_at" -> EJsString,
      "actor" -> extractUser,
      "payload" -> EJsObject(
        "ref_type" -> JsString("branch"),
        "ref" -> EJsString
      ),
      "repo" -> extractRepo
    )
  )

  object DeleteBranchEvent extends JsonExtractor(
    EJsObject(
      "type" -> JsString("DeleteEvent"),
      "created_at" -> EJsString,
      "actor" -> extractUser,
      "payload" -> EJsObject(
        "ref_type" -> JsString("branch"),
        "ref" -> EJsString
      ),
      "repo" -> extractRepo
    )
  )

  object MemberEvent extends JsonExtractor(
    EJsObject(
      "type" -> JsString("MemberEvent"),
      "created_at" -> EJsString,
      "payload" -> EJsObject(
        "member" -> extractUser,
        "action" -> EJsString
      ),
      "repo" -> extractRepo
    )
  )

  object IssuesEvent extends JsonExtractor(
    EJsObject(
      "type" -> JsString("IssuesEvent"),
      "created_at" -> EJsString,
      "actor" -> extractUser,
      "payload" -> EJsObject(
        "action" -> EJsString,
        "issue" -> EJsObject(
          "number" -> EJsNumber
        )
      ),
      "repo" -> extractRepo
    )
  )

  object IssueCommentEvent extends JsonExtractor(
    EJsObject(
      "type" -> JsString("IssueCommentEvent"),
      "created_at" -> EJsString,
      "actor" -> extractUser,
      "payload" -> EJsObject(
        "issue" -> EJsObject(
          "number" -> EJsNumber
        )
      ),
      "repo" -> extractRepo
    )
  )

  object PullRequestEvent extends JsonExtractor(
    EJsObject(
      "type" -> JsString("PullRequestEvent"),
      "created_at" -> EJsString,
      "actor" -> extractUser,
      "payload" -> EJsObject(
        "action" -> EJsString,
        "number" -> EJsNumber
      ),
      "repo" -> extractRepo
    )
  )

  object ForkEvent extends JsonExtractor(
    EJsObject(
      "type" -> JsString("ForkEvent"),
      "created_at" -> EJsString,
      "actor" -> extractUser,
      "repo" -> extractRepo
    )
  )

  object WatchEvent extends JsonExtractor(
    EJsObject(
      "type" -> JsString("WatchEvent"),
      "created_at" -> EJsString,
      "actor" -> extractUser,
      "repo" -> extractRepo
    )
  )

  object GenericRepoEvent extends JsonExtractor(
    EJsObject(
      "type" -> EJsString,
      "created_at" -> EJsString,
      "repo" -> extractRepo
    )
  )

  implicit object EventFormat extends ReadOnlyJsonFormat[(Instant, Status)] {
    def read(value: JsValue) = value match {
      case PushEvent(Seq(createdAt: String, login: String, size, repo: String)) =>
        (Instant.parse(createdAt), Status(User(login), s"$login pushed $size commit(s) in repository $repo", repoUrl(repo)))
      case CreateRepositoryEvent(Seq(createdAt: String, login: String, repo: String)) =>
        (Instant.parse(createdAt), Status(User(login), s"$login created repository $repo", repoUrl(repo)))
      case CreateBranchEvent(Seq(createdAt: String, login: String, branch, repo: String)) =>
        (Instant.parse(createdAt), Status(User(login), s"$login created branch $branch in repository $repo", repoUrl(repo)))
      case DeleteBranchEvent(Seq(createdAt: String, login: String, branch, repo: String)) =>
        (Instant.parse(createdAt), Status(User(login), s"$login deleted branch $branch in repository $repo", repoUrl(repo)))
      case MemberEvent(Seq(createdAt: String, member: String, action, repo: String)) =>
        (Instant.parse(createdAt), Status(User(member), s"Member $member $action to repository $repo", repoUrl(repo)))
      case IssuesEvent(Seq(createdAt: String, login: String, action, issue, repo: String)) =>
        (Instant.parse(createdAt), Status(User(login), s"$login $action issue $issue in repository $repo", repoUrl(repo)))
      case IssueCommentEvent(Seq(createdAt: String, login: String, issue, repo: String)) =>
        (Instant.parse(createdAt), Status(User(login), s"$login commented on issue $issue in repository $repo", repoUrl(repo)))
      case PullRequestEvent(Seq(createdAt: String, login: String, action, pr, repo: String)) =>
        (Instant.parse(createdAt), Status(User(login), s"$login $action pull request $pr in repository $repo", repoUrl(repo)))
      case ForkEvent(Seq(createdAt: String, login: String, repo: String)) =>
        (Instant.parse(createdAt), Status(User(login), s"$login forked repository $repo", repoUrl(repo)))
      case WatchEvent(Seq(createdAt: String, login: String, repo: String)) =>
        (Instant.parse(createdAt), Status(User(login), s"$login starred repository $repo", repoUrl(repo)))
      case GenericRepoEvent(Seq(eventType: String, createdAt: String, repo: String)) =>
        (Instant.parse(createdAt), Status(User("unknown"), s"$eventType in repository $repo", repoUrl(repo)))
      case _ => throw DeserializationException(s"""Unsupported event JSON: $value""")
    }

  }

}
