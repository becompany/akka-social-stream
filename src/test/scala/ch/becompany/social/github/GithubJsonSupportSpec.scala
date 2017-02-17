package ch.becompany.social.github

import java.time.Instant

import ch.becompany.json.Codecs._
import com.typesafe.scalalogging.LazyLogging
import io.circe.generic.auto._
import io.circe.parser._
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{EitherValues, FlatSpec, Matchers}

class GithubJsonSupportSpec extends FlatSpec with Matchers with LazyLogging with EitherValues with TypeCheckedTripleEquals {

  "GithubJsonSupport" should "match JSON" in {
    val now = Instant.now
    val event = PushEvent(now)

    val rawJson: String = s"""
                             |{
                             |  "created_at": "$now",
                             |  "type": "PushEvent"
                             |}
                             |""".stripMargin

    parse(rawJson).right.value.as[PushEvent].right.value should === (event)
  }

}
