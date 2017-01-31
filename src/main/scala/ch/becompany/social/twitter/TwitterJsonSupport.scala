package ch.becompany.social.twitter

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import ch.becompany.social.{Status, User}
import com.twitter.Autolink
import spray.json._

import scalatags.Text.all._

case class UserId(id: String)

trait TwitterJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit object TweetJsonFormat extends JsonFormat[(Instant, Status)] {

    private val dateParser = DateTimeFormatter.
      ofPattern("EEE MMM dd HH:mm:ss Z yyyy").
      withLocale(Locale.ENGLISH)

    private def parseDate(s: String): Instant =
      Instant.from(dateParser.parse(s))

    private def userLink(screenName: String): String =
      s"https://twitter.com/$screenName"

    private def tweetLink(id: String): String =
      s"https://twitter.com/statuses/$id"

    private val autolink = new Autolink

    private def tweetHtml(text: String): String =
      autolink.autoLink(text)

    override def write(obj: (Instant, Status)): JsValue =
      throw new SerializationException("not supported")

    override def read(json: JsValue): (Instant, Status) =
      json.asJsObject.getFields("id_str", "created_at", "text", "user") match {
        case Seq(JsString(id), JsString(createdAt), JsString(text), user: JsObject) =>
          user.getFields("screen_name", "name") match {
            case Seq(JsString(screenName), JsString(name)) =>
              (parseDate(createdAt), Status(
                author = User(screenName, userLink(screenName), Option(name)),
                html = raw(tweetHtml(text)))
              )
            case _ => throw DeserializationException("invalid user JSON")
          }
        case _ => throw DeserializationException("invalid tweet JSON")
      }

  }

  implicit object UserIdFormat extends RootJsonFormat[UserId] {
    override def write(obj: UserId): JsValue =
      throw new SerializationException("not supported")

    override def read(json: JsValue): UserId =
      json.asJsObject.getFields("id_str") match {
        case Seq(JsString(id)) => UserId(id)
        case _ => throw DeserializationException("invalid user JSON")
      }
  }

}
