package ch.becompany.social.github

import java.time.Instant

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.testkit.scaladsl.TestSink
import ch.becompany.social.Status
import org.scalatest.FlatSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Try}

class GithubFeedSpec extends FlatSpec {

  implicit val system = ActorSystem("github-feed-spec")
  implicit val materializer = ActorMaterializer()

  "Github event feed" should "stream GitHub events" in {
    val feed = new GithubFeed("becompany")

    feed.stream().
      map(t => { println(t); t }).
      runWith(TestSink.probe[(Instant, Try[Status])]).
      request(30).
      expectNextChainingPF {
        case (date, Success(test)) => println(date, test)
      }
  }

}
