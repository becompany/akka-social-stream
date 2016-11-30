package ch.becompany.social.github

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.testkit.scaladsl.TestSink
import ch.becompany.social.{Status, User}
import org.scalatest.FlatSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Try}

class GithubEventsFeedSpec extends FlatSpec {

  implicit val system = ActorSystem("twitter-feed-spec")
  implicit val materializer = ActorMaterializer()

  "Github event feed" should "stream github events" in {
    val feed = new GithubEventsFeed("becompany")

    feed.source(5).
      runWith(TestSink.probe[Try[Status]]).
      request(20).
      expectNextChainingPF {
        case Success(test) => println(test)
      }
  }

}
