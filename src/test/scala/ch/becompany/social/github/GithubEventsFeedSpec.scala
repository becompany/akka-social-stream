package ch.becompany.social.github

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.testkit.scaladsl.TestSink
import ch.becompany.social.Status
import com.typesafe.config.ConfigFactory
import org.scalatest.FlatSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Try}

class GithubEventsFeedSpec extends FlatSpec {

  implicit val system = ActorSystem("github-feed-spec")
  implicit val materializer = ActorMaterializer()

  "Github event feed" should "stream github events" in {
    val feed = new GithubEventsFeed("becompany")

    feed.source(30).
      map(t => { t.map(s => println(s.date, s.text, s.link)); t}).
      runWith(TestSink.probe[Try[Status]]).
      request(30).
      expectNextChainingPF {
        case Success(test) => println(test)
      }
  }

}
