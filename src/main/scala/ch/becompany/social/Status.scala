package ch.becompany.social

import scalatags.Text.TypedTag

case class User(username: String, name: Option[String] = None)

case class Status(author: User, html: TypedTag[String])
