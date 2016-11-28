package ch.becompany.social.github

case class User(
  login: String
)

case class Repository(
  id: Long,
  name: String,
  owner: User
)

case class CommitPage(
  url: String,
  commit: Commit
)

case class Commit(
  message: String
)

case class Event(
  `type`: String,
  created_at: String,
  actor: User
)