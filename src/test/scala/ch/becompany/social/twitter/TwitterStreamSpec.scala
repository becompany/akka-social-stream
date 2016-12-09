package ch.becompany.social.twitter

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.testkit.scaladsl.TestSink
import ch.becompany.social.Status
import org.scalatest.{FlatSpec, Inspectors, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Try}

class TwitterStreamSpec extends FlatSpec with Matchers with Inspectors {

  implicit val system = ActorSystem("twitter-stream-spec")
  implicit val materializer = ActorMaterializer()

  val keyword = "happy"
  val n = 100

  "Twitter stream" should "stream tweets" in {

    val results = TwitterStream("track" -> keyword).
      stream.
      runWith(TestSink.probe[Try[Status]]).
      request(n).
      expectNextN(n)

    forAll(results)(_ should matchPattern { case Success(Status(_, _, _, _)) => })
  }

}
