package ch.becompany.social.github

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest.FlatSpec

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class GithubClientSpec extends FlatSpec {

  import GithubClient._

  "A github client" should "receive org repos" in {
    val f = repositories("becompany")
    f foreach println
    assert(Await.result(f, 10 seconds).size == 30)
  }
/*
  it should "receive commits" in {
    val f = repositories("becompany").flatMap { repos =>
      Future.sequence(
      repos.map { repo =>
        commits(repo.owner.login, repo.name)
      })
    }
    f foreach println
    Await.result(f, 10 seconds)
  }
*/
  it should "receive events" in {
    val f = events("becompany")
    f foreach println
    assert(Await.result(f, 10 seconds).size == 30)
  }

}
