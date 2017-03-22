package ch.becompany.social.facebook

import com.typesafe.scalalogging.LazyLogging
import org.scalatest.FlatSpec

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class FacebookClientSpec extends FlatSpec with LazyLogging {

  "A facebook client" should "receive posts" in {
    val f = FacebookClient.posts("117198891659771")
    f.map(_.mkString("\n")).foreach(e => logger.info(e))
    assert(Await.result(f, 10 seconds).nonEmpty)
  }

  "A facebook client" should "receive empty list if request fails" in {
    val f = FacebookClient.posts("NOT_EXIST")
    f.map(_.mkString("\n")).foreach(e => logger.info(e))
    assert(Await.result(f, 10 seconds).isEmpty)
  }

}
