package ch.becompany.social.github

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import ch.becompany.json._
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

  implicit object EventFormat extends ReadOnlyJsonFormat[Event] {
    def read(value: JsValue) = value match {
      case PushEvent(Seq(createdAt: String, login, size, repo)) =>
        Event(createdAt, s"$login pushed $size commit(s) in repository $repo")
      case CreateRepositoryEvent(Seq(createdAt: String, login, repo)) =>
        Event(createdAt, s"$login created repository $repo")
      case CreateBranchEvent(Seq(createdAt: String, login, branch, repo)) =>
        Event(createdAt, s"$login created branch $branch in repository $repo")
      case DeleteBranchEvent(Seq(createdAt: String, login, branch, repo)) =>
        Event(createdAt, s"$login deleted branch $branch in repository $repo")
      case MemberEvent(Seq(createdAt: String, member, action, repo)) =>
        Event(createdAt, s"Member $member $action to repository $repo")
      case IssuesEvent(Seq(createdAt: String, login, action, issue, repo)) =>
        Event(createdAt, s"$login $action issue $issue in repository $repo")
      case IssueCommentEvent(Seq(createdAt: String, login, issue, repo)) =>
        Event(createdAt, s"$login commented on issue $issue in repository $repo")
      case PullRequestEvent(Seq(createdAt: String, login, action, pr, repo)) =>
        Event(createdAt, s"$login $action pull request $pr in repository $repo")
      case _ => throw DeserializationException(s"""Unsupported event JSON: $value""")
    }

  }

}
