package ch.becompany.social.github

import java.time.Instant

import ch.becompany.json.JsonDecoder
import ch.becompany.social.github.Events._
import com.typesafe.scalalogging.LazyLogging
import io.circe.parser._
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{EitherValues, FlatSpec, Matchers}

class GithubJsonSupportSpec extends FlatSpec with Matchers with LazyLogging with EitherValues with TypeCheckedTripleEquals {

  import ch.becompany.json.Codecs._
  import io.circe.generic.auto._

  "GithubJsonSupport" should "match a PushEvent" in {

    val now = Instant.parse("2011-09-06T17:26:27Z")
    val event = PushEvent(now, User("devkat"), PushEventPayload(10, "foo"), Repository("repo-name"))

    val rawJson: String = s"""
                             |{
                             |  "created_at": "2011-09-06T17:26:27Z",
                             |  "type": "PushEvent",
                             |  "actor": {
                             |    "login": "devkat"
                             |  },
                             |  "payload": {
                             |    "size": 10,
                             |    "ref": "foo"
                             |  },
                             |  "repo": {
                             |    "name": "repo-name"
                             |  }
                             |}
                             |""".stripMargin

    val json = parse(rawJson).right.value
    val parsed = implicitly[JsonDecoder[GithubEvent]].extract(json)

    parsed should === (Some(event))
  }

  "GithubJsonSupport" should "match a CreateEvent" in {

    val now = Instant.parse("2011-09-06T17:26:27Z")
    val event = CreateEvent(now, User("devkat"), Reference("branch", "foo-ref"), Repository("repo-name"))

    val rawJson: String = s"""
                             |{
                             |  "created_at": "2011-09-06T17:26:27Z",
                             |  "type": "CreateEvent",
                             |  "actor": {
                             |    "login": "devkat"
                             |  },
                             |  "payload": {
                             |    "ref_type": "branch",
                             |    "ref": "foo-ref"
                             |  },
                             |  "repo": {
                             |    "name": "repo-name"
                             |  }
                             |}
                             |""".stripMargin

    val json = parse(rawJson).right.value
    val parsed = implicitly[JsonDecoder[GithubEvent]].extract(json)

    parsed should === (Some(event))
  }

}
