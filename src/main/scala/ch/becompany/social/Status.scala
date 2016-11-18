package ch.becompany.social

import java.time.Instant

case class Status(author: String, date: Instant, text: String, link: String)
