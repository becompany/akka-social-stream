package ch.becompany.social.github

import com.typesafe.scalalogging.LazyLogging
import org.scalatest.FlatSpec

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class GithubClientSpec extends FlatSpec with LazyLogging {

  import GithubClient._

  "A github client" should "receive events" in {
    val f = events("becompany")
    f.map(_.mkString("\n")).foreach(e => logger.info(e))
    assert(Await.result(f, 10 seconds).size == 30)
  }

}
