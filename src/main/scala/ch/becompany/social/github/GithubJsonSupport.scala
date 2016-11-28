package ch.becompany.social.github

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait GithubJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val userFormat = jsonFormat1(User)
  implicit val repositoryFormat = jsonFormat3(Repository)
  implicit val commitFormat = jsonFormat1(Commit)
  implicit val commitPageFormat = jsonFormat2(CommitPage)
  implicit val eventFormat = jsonFormat3(Event)

}
