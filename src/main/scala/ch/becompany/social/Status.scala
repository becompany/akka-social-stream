package ch.becompany.social

import scalatags.Text.Frag

case class User(username: String, url: String, name: Option[String] = None)

case class Status(author: User, html: Frag)
