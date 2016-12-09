package ch.becompany.social.twitter

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest.{FlatSpec, Inspectors, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class TwitterClientSpec extends FlatSpec with Matchers with Inspectors {

  implicit val system = ActorSystem("twitter-client-spec")
  implicit val materializer = ActorMaterializer()

  val username = "twittersuggests"

  "Twitter client" should "stream tweets" in {

    val f = new TwitterClient(username).latest(5)
    f map(_.mkString("\n")) foreach println
    val result = Await.result(f, 10 seconds)
    result.size shouldBe 5
    forAll(result)(_.author.username shouldBe username)
  }

}
