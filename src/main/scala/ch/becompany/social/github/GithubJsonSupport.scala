package ch.becompany.social.github

import java.time.Instant

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import ch.becompany.json._
import ch.becompany.social.{Status, User}
import spray.json._

import scalatags.Text.TypedTag
import scalatags.Text.all._

trait ReadOnlyJsonFormat[T] extends JsonFormat[T] {
  def write(e: T) = throw new SerializationException("not supported")
}

/**
  * Convert GitHub events JSON to status messages.
  * @see https://github.com/christianvuerings/jquery-lifestream/blob/master/src/services/github.js
  */
trait GithubJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  import JsonExtractor._

  val extractUser = EJsObject("login" -> EJsString)
  val extractRepo = EJsObject("name" -> EJsString)

  private def userUrl(user: String) =
    s"https://github.com/$user"

  private def userLink(user: String) =
    a(href := userUrl(user))(user)

  private def refLink(repo: String, ref: String) =
    a(href := s"https://github.com/$repo/tree/$ref")(ref)

  private def repoLink(repo: String) =
    a(href := s"https://github.com/$repo")(repo)

  private def issueLink(repo: String, issue: BigDecimal) =
    a(href := s"https://github.com/$repo/issues/$issue")(issue.toString)

  private def pullRequestLink(repo: String, number: BigDecimal) =
    a(href := s"https://github.com/$repo/pull/$number")(number.toString)

  object PushEvent extends JsonExtractor(
    EJsObject(
      "type" -> JsString("PushEvent"),
      "created_at" -> EJsString,
      "actor" -> extractUser,
      "payload" -> EJsObject(
        "size" -> EJsNumber,
        "ref" -> EJsString
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

  object CreateTagEvent extends JsonExtractor(
    EJsObject(
      "type" -> JsString("CreateEvent"),
      "created_at" -> EJsString,
      "actor" -> extractUser,
      "payload" -> EJsObject(
        "ref_type" -> JsString("tag"),
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

  def status(createdAt: String, login: String, html: TypedTag[String]): (Instant, Status) =
    (Instant.parse(createdAt), Status(User(login, userUrl(login)), html))

  implicit object EventFormat extends ReadOnlyJsonFormat[(Instant, Status)] {
    def read(value: JsValue) = value match {

      case CreateBranchEvent(Seq(createdAt: String, login: String, ref: String, repo: String)) =>
        status(createdAt, login, span(userLink(login), s" created branch ", refLink(repo, ref), " in repository ", repoLink(repo)))

      case CreateRepositoryEvent(Seq(createdAt: String, login: String, repo: String)) =>
        status(createdAt, login, span(userLink(login), s" created repository ", repoLink(repo)))

      case CreateTagEvent(Seq(createdAt: String, login: String, ref: String, repo: String)) =>
        status(createdAt, login, span(userLink(login), s" created tag ", refLink(repo, ref), " in repository ", repoLink(repo)))

      case DeleteBranchEvent(Seq(createdAt: String, login: String, branch, repo: String)) =>
        status(createdAt, login, span(userLink(login), s" deleted branch $branch in repository ", repoLink(repo)))

      case ForkEvent(Seq(createdAt: String, login: String, repo: String)) =>
        status(createdAt, login, span(userLink(login), s" forked repository ", repoLink(repo)))

      case IssueCommentEvent(Seq(createdAt: String, login: String, issue: BigDecimal, repo: String)) =>
        status(createdAt, login, span(userLink(login), s" commented on issue ", issueLink(repo, issue), " in repository ", repoLink(repo)))

      case IssuesEvent(Seq(createdAt: String, login: String, action: String, issue: BigDecimal, repo: String)) =>
        status(createdAt, login, span(userLink(login), s" $action issue ", issueLink(repo, issue), " in repository ", repoLink(repo)))

      case MemberEvent(Seq(createdAt: String, member: String, action: String, repo: String)) =>
        status(createdAt, member, span(userLink(member), s" $action to repository ", repoLink(repo)))

      case PullRequestEvent(Seq(createdAt: String, login: String, action: String, number: BigDecimal, repo: String)) =>
        status(createdAt, login, span(userLink(login), s" $action pull request ", pullRequestLink(repo, number), " in repository ", repoLink(repo)))

      case PushEvent(Seq(createdAt: String, login: String, size, ref: String, repo: String)) =>
        status(createdAt, login, span(userLink(login), s" pushed $size commit(s) to ", refLink(repo, ref), " in repository ", repoLink(repo)))

      case WatchEvent(Seq(createdAt: String, login: String, repo: String)) =>
        status(createdAt, login, span(userLink(login), s" starred repository ", repoLink(repo)))

      case GenericRepoEvent(Seq(eventType: String, createdAt: String, repo: String)) =>
        status(createdAt, "unknown" , span(s"$eventType in repository ", repoLink(repo)))

      case _ => throw DeserializationException(s"""Unsupported event JSON: $value""")
    }

  }

}
