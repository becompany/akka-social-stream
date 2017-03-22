package ch.becompany.social.github

import java.time.Instant

object Events {

  case class User(login: String)
  case class Repository(name: String)

  sealed abstract class GithubEvent(created_at: Instant, `type`: String = this.getClass.getSimpleName)

  case class PushEventPayload(size: Long, ref: String)

  case class PushEvent(created_at: Instant,
                       actor: User,
                       payload: PushEventPayload,
                       repo: Repository) extends GithubEvent(created_at)

  case class Reference(ref_type: String, ref: String)

  case class CreateEvent(created_at: Instant,
                         actor: User,
                         payload: Reference,
                         repo: Repository) extends GithubEvent(created_at)

  case class DeleteEvent(created_at: Instant,
                         actor: User,
                         payload: Reference,
                         repo: Repository) extends GithubEvent(created_at)

  case class Issue(number: Long)

  case class IssueCommentEvent(created_at: Instant,
                               actor: User,
                               payload: Issue,
                               repo: Repository) extends GithubEvent(created_at)

}
