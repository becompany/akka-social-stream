package ch.becompany.social

case class User(username: String, name: Option[String] = None)

case class Status(author: User, text: String, link: String)
