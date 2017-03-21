package ch.becompany.social.facebook

import java.time.Instant

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import ch.becompany.social.{Status, User}
import com.typesafe.config.ConfigFactory
import spray.json._

import scalatags.Text.all._


trait FacebookJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  private val config = ConfigFactory.load.getConfig("scalaSocialFeed.facebook.author")
  private val author = (config.hasPath("name"), config.hasPath("link")) match {
    case (true, true) => User(config.getString("name"), config.getString("link"))
    case _ => User("Unknown", "#")
  }

  implicit object FacebookJsonFormat extends RootJsonReader[List[(Instant, Status)]] {
    override def read(json: JsValue): List[(Instant, Status)] =
      json.asJsObject.getFields("data") match {
        case posts: Seq[JsValue] => posts(0) match {
          case JsArray(array) =>
            array.map( post => {
              post.asJsObject.getFields("message", "created_time") match {
                case Seq(JsString(message), JsString(createdTime)) =>
                  Some(
                    (Instant.parse(createdTime.split("\\+")(0)+"Z"), Status(author, span(message)))
                  )
                case _ => Option.empty
              }
            }).flatten.toList

          case _ => throw new DeserializationException("Invalid JSON.")

        }
        case _ => throw new DeserializationException("Invalid JSON.")
      }
  }

}
