package ch.becompany.social.twitter

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.testkit.scaladsl.TestSink
import ch.becompany.social.Status
import org.scalatest.FlatSpec

import scala.util.{Success, Try}

class TwitterFeedSpec extends FlatSpec {

  implicit val system = ActorSystem("twitter-feed-spec")
  implicit val materializer = ActorMaterializer()

  "Twitter feed" should "stream tweets" in {

    val feed = new TwitterFeed(Some("BeCompany_CH"))

    feed.stream(5).
      runWith(TestSink.probe[Try[Status]]).
      request(20).
      expectNextChainingPF {
        case Success(Status("BeCompany GmbH", _, _)) =>
      }

  }

}
