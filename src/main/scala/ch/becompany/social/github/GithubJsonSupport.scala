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

  implicit object EventFormat extends ReadOnlyJsonFormat[Status] {
    def read(value: JsValue) = value match {
      case PushEvent(Seq(createdAt: String, login: String, size, repo)) =>
        Status(User(login, null), Instant.parse(createdAt), s"$login pushed $size commit(s) in repository $repo", null)
      case CreateRepositoryEvent(Seq(createdAt: String, login: String, repo)) =>
        Status(User(login, null), Instant.parse(createdAt), s"$login created repository $repo", null)
      case CreateBranchEvent(Seq(createdAt: String, login: String, branch, repo)) =>
        Status(User(login, null), Instant.parse(createdAt), s"$login created branch $branch in repository $repo", null)
      case DeleteBranchEvent(Seq(createdAt: String, login: String, branch, repo)) =>
        Status(User(login, null), Instant.parse(createdAt), s"$login deleted branch $branch in repository $repo", null)
      case MemberEvent(Seq(createdAt: String, member: String, action, repo)) =>
        Status(User(member, null), Instant.parse(createdAt), s"Member $member $action to repository $repo", null)
      case IssuesEvent(Seq(createdAt: String, login: String, action, issue, repo)) =>
        Status(User(login, null), Instant.parse(createdAt), s"$login $action issue $issue in repository $repo", null)
      case IssueCommentEvent(Seq(createdAt: String, login: String, issue, repo)) =>
        Status(User(login, null), Instant.parse(createdAt), s"$login commented on issue $issue in repository $repo", null)
      case PullRequestEvent(Seq(createdAt: String, login: String, action, pr, repo)) =>
        Status(User(login, null), Instant.parse(createdAt), s"$login $action pull request $pr in repository $repo", null)
      case _ => throw DeserializationException(s"""Unsupported event JSON: $value""")
    }

  }

}
