package ch.becompany.social.github

import java.time.Instant

sealed abstract class GithubEvent(created_at: Instant, `type`: String)

case class PushEvent(created_at: Instant) extends GithubEvent(created_at, "PushEvent")