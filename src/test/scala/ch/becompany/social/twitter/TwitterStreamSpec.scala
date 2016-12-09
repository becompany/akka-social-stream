package ch.becompany.social.twitter

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.testkit.scaladsl.TestSink
import ch.becompany.social.Status
import org.scalatest.FlatSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Try}

class TwitterStreamSpec extends FlatSpec {

  implicit val system = ActorSystem("twitter-stream-spec")
  implicit val materializer = ActorMaterializer()

  "Twitter stream" should "stream tweets" in {

    TwitterStream("track" -> "happy").
      stream.
      map(t => {println(t); t}).
      runWith(TestSink.probe[Try[Status]]).
      request(20).
      expectNextChainingPF {
        case Success(Status(_, _, _, _)) =>
      }

  }

}
