package ch.becompany.social.twitter

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.testkit.scaladsl.TestSink
import ch.becompany.social.{Status, User}
import org.scalatest.FlatSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Try}

class TwitterFeedSpec extends FlatSpec {

  implicit val system = ActorSystem("twitter-feed-spec")
  implicit val materializer = ActorMaterializer()

  val user = "twittersuggests"

  "Twitter feed" should "stream tweets" in {

    val feed = TwitterFeed("track" -> "happy")

    feed.source(5).
      map(t => {println(t); t}).
      runWith(TestSink.probe[Try[Status]]).
      request(20).
      expectNextChainingPF {
        case Success(Status(_, _, _, _)) =>
      }

  }

}
