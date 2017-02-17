package ch.becompany.social.github

import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter

import ch.becompany.json.Codecs._
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.{EitherValues, FlatSpec, Matchers}
import io.circe._
import io.circe.parser._
import io.circe.generic.auto._
import org.scalactic.TypeCheckedTripleEquals

class GithubJsonSupportSpec extends FlatSpec with Matchers with LazyLogging with EitherValues with TypeCheckedTripleEquals {

  val dateFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.systemDefault)

  "GithubJsonSupport" should "match JSON" in {

    val now = Instant.now
    val event = PushEvent(now)

    val rawJson: String = s"""
                             |{
                             |  "created_at": "${dateFormatter.format(now)}",
                             |  "type": "PushEvent"
                             |}
                             |""".stripMargin

    println(parse(rawJson).right.value.as[PushEvent])
    parse(rawJson).right.value.as[PushEvent].right.value should === (event)
  }

}
