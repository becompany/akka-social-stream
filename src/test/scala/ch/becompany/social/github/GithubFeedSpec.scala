package ch.becompany.social.github

import java.time.Instant

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.testkit.scaladsl.TestSink
import ch.becompany.social.Status
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.FlatSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Try}

class GithubFeedSpec extends FlatSpec with LazyLogging {

  implicit val system = ActorSystem("github-feed-spec")
  implicit val materializer = ActorMaterializer()

  "Github event feed" should "stream GitHub events" in {
    val feed = new GithubFeed("becompany")

    feed.stream().
      map(t => { logger.info(t.toString); t }).
      runWith(TestSink.probe[(Instant, Try[Status])]).
      request(30).
      expectNextChainingPF {
        case (date, Success(test)) => logger.info(s"$$date $test")
      }
  }

}
