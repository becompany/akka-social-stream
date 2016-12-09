package ch.becompany.social.twitter

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import ch.becompany.social.{Status, User}
import spray.json._

trait TwitterJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit object TweetJsonFormat extends JsonFormat[Status] {

    private val dateParser = DateTimeFormatter.
      ofPattern("EEE MMM dd HH:mm:ss Z yyyy").
      withLocale(Locale.ENGLISH)

    private def parseDate(s: String): Instant =
      Instant.from(dateParser.parse(s))

    private def link(id: String): String =
      s"https://twitter.com/statuses/$id"

    override def write(obj: Status): JsValue =
      throw new SerializationException("not supported")

    override def read(json: JsValue): Status =
      json.asJsObject.getFields("id_str", "created_at", "text", "user") match {
        case Seq(JsString(id), JsString(createdAt), JsString(text), user: JsObject) =>
          user.getFields("screen_name", "name") match {
            case Seq(JsString(screenName), JsString(name)) =>
              Status(
                author = User(screenName, Option(name)),
                date = parseDate(createdAt),
                text = text,
                link = link(id))
            case _ => throw DeserializationException("invalid user JSON")
          }
        case _ => throw DeserializationException("invalid tweet JSON")
      }

  }

}
