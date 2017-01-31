package ch.becompany.social.twitter

import java.time.Instant

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.testkit.scaladsl.TestSink
import ch.becompany.social.Status
import com.typesafe.scalalogging.LazyLogging
import org.scalatest.{FlatSpec, Inspectors, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Try}

class TwitterStreamSpec extends FlatSpec with Matchers with Inspectors with LazyLogging {

  import ch.becompany.util.StreamLogging._

  implicit val system = ActorSystem("twitter-stream-spec")
  implicit val materializer = ActorMaterializer()

  "Twitter stream" should "stream tweets by keyword" in {

    val results = TwitterStream("track" -> "happy").
      stream.
      via(logElements).
      runWith(TestSink.probe[(Instant, Try[Status])]).
      request(20).
      expectNextN(20)

    forAll(results)(_ should matchPattern { case (_, Success(Status( _, _))) => })
  }

  "Twitter stream" should "stream tweets by user ID" in {

    val results = TwitterStream("follow" -> "20536157").
      stream.
      via(logElements).
      runWith(TestSink.probe[(Instant, Try[Status])]).
      request(2).
      expectNextN(2)

    forAll(results)(_ should matchPattern { case (_, Success(Status(_, _))) => })
  }

  "Twitter stream" should "retry when an error occurs" in {

    val results = new TwitterStream(Map("follow" -> "20536157"), "notfound").
      stream.
      via(logElements).
      runWith(TestSink.probe[(Instant, Try[Status])]).
      request(2).
      expectNextN(2)

    forAll(results)(_ should matchPattern { case (_, Success(Status(_, _))) => })
  }

}
