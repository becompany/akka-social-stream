package ch.becompany.social

import java.time.Instant

case class User(username: String, name: String)

case class Status(author: User, date: Instant, text: String, link: String)
