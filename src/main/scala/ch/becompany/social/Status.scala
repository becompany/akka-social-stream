package ch.becompany.social

import java.time.Instant

case class User(username: String, name: Option[String] = None)

case class Status(author: User, date: Instant, text: String, link: String)
