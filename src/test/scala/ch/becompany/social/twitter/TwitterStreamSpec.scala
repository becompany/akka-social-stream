package ch.becompany.social.twitter

import java.time.Instant

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

  val n = 100

  "Twitter stream" should "stream tweets by keyword" in {

    val results = TwitterStream("track" -> "happy").
      stream.
      runWith(TestSink.probe[(Instant, Try[Status])]).
      request(n).
      expectNextN(n)

    forAll(results)(_ should matchPattern { case (_, Success(Status( _, _))) => })
  }

  "Twitter stream" should "stream tweets by user ID" in {

    val results = TwitterStream("follow" -> "20536157").
      stream.
      map(t => { println(t); t }).
      runWith(TestSink.probe[(Instant, Try[Status])]).
      request(n).
      expectNextN(n)

    forAll(results)(_ should matchPattern { case (_, Success(Status(_, _))) => })
  }

}
