package ch.becompany.social.twitter

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpHeader.ParsingResult
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import ch.becompany.social.{Status, User}
import com.hunorkovacs.koauth.domain.KoauthRequest
import com.hunorkovacs.koauth.service.consumer.DefaultConsumerService
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods.parse

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Try}

class TwitterStream(filter: Map[String, String])(implicit ec: ExecutionContext) {

  private val url = "https://stream.twitter.com/1.1/statuses/filter.json"

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val formats = DefaultFormats

  private val conf = OAuthConfig.load

  private val consumer = new DefaultConsumerService(system.dispatcher)

  private val source = Uri(url)

  private val dateParser = DateTimeFormatter.
    ofPattern("EEE MMM dd HH:mm:ss Z yyyy").
    withLocale(Locale.ENGLISH)

  private def parseDate(s: String): Instant =
    Instant.from(dateParser.parse(s))

  private def link(tweet: Tweet): String =
    s"https://twitter.com/statuses/${tweet.id_str}"

  private def oauthHeader: Future[String] =
    consumer.createOauthenticatedRequest(
      KoauthRequest(
        method = "POST",
        url = url,
        authorizationHeader = None,
        body = Some(filter.map{ case (k, v) => s"$k=$v" }.mkString("&"))),
      conf.consumerKey, conf.consumerSecret, conf.accessToken, conf.accessTokenSecret).
      map(_.header)

  private def toStatus(tweet: Tweet): Status =
    Status(
      author = User(tweet.user.screen_name, Option(tweet.user.name)),
      date = parseDate(tweet.created_at),
      text = tweet.text,
      link = link(tweet))

  private def request: Future[Source[Try[Status], Any]] =
    oauthHeader.flatMap { header =>
      val httpHeaders: List[HttpHeader] = List(
        HttpHeader.parse("Authorization", header) match {
          case ParsingResult.Ok(h, _) => Some(h)
          case _ => None
        },
        HttpHeader.parse("Accept", "*/*") match {
          case ParsingResult.Ok(h, _) => Some(h)
          case _ => None
        }
      ).flatten
      val httpRequest: HttpRequest = HttpRequest(
        method = HttpMethods.POST,
        uri = source,
        headers = httpHeaders,
        entity = FormData(filter).toEntity
      )
      Http().singleRequest(httpRequest).flatMap { response =>
        if (response.status == StatusCodes.OK) {
          Future(response.entity.dataBytes
            .scan("")((acc, curr) => if (acc.contains("\r\n")) curr.utf8String else acc + curr.utf8String)
            .filter(_.contains("\r\n"))
            .map(json => Try(parse(json).extract[Tweet]).map(toStatus)))
        } else {
          response.entity.toStrict(5 seconds).
            map(_.data.utf8String).
            map(msg => Source.single(Failure(new IllegalStateException(msg))))
        }
      }
    }.recover {
      case e => Source.single(Failure(e))
    }

  def stream: Source[Try[Status], Any] =
    Source.fromFuture(request).flatMapConcat(identity)

}
