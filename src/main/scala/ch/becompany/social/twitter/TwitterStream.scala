package ch.becompany.social.twitter

import java.io.IOException
import java.time.Instant

import akka.http.scaladsl.model._
import akka.stream.scaladsl.{Framing, Source}
import akka.util.ByteString
import ch.becompany.http.{HttpClient, HttpHandler}
import ch.becompany.social.Status
import com.typesafe.scalalogging.LazyLogging
import spray.json._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

class TwitterStream(filter: Map[String, String])(implicit ec: ExecutionContext)
  extends HttpClient
    with TwitterOAuthSupport
    with TwitterJsonSupport
    with LazyLogging {

  private val url = "https://stream.twitter.com/1.1/statuses/filter.json"

  private val source = Uri(url)

  private def httpRequest(): HttpRequest = HttpRequest(
    method = HttpMethods.POST,
    uri = source,
    headers = List(headers.Accept(MediaRanges.`*/*`)),
    entity = FormData(filter).toEntity
  )

  private def httpSuccessSource(response: HttpResponse): Future[Source[(Instant, Try[Status]), Any]] =
    Future(response.entity.dataBytes.
      via(Framing.delimiter(ByteString("\r\n"), maximumFrameLength = Int.MaxValue, allowTruncation = true)).
      map(_.utf8String).
      filter(_.length > 0).
      map { json =>
        val (date, status) = TweetJsonFormat.read(json.parseJson)
        (date, Try(status))
      }
    )

  private def httpErrorSource(response: HttpResponse): Future[Source[(Instant, Try[Status]), Any]] =
    response.entity.toStrict(5 seconds).
      map(_.data.utf8String).
      map { msg =>
        logger.error(s"Response ${response.status}: $msg")
        Source.single(Instant.now -> Failure(new IOException(msg)))
      }

  implicit object handler extends HttpHandler[Source[(Instant, Try[Status]), Any]] {
    override def handle(request: HttpRequest, response: HttpResponse)
                       (implicit ec: ExecutionContext): Future[Source[(Instant, Try[Status]), Any]] =
      response.status match {
        case StatusCodes.OK => httpSuccessSource(response)
        case _ => httpErrorSource(response)
      }
  }

  def stream: Source[(Instant, Try[Status]), Any] = {
    logger.debug(s"Streaming tweets with filter $filter")
    // Emit error instead of failing the complete stream
    val futureSrc = req(httpRequest()).recover {
      case e => Source.single(Instant.now -> Failure(e))
    }
    Source.fromFuture(futureSrc).flatMapConcat(identity)
  }

}

object TwitterStream {

  def apply(filter: (String, String)*)(implicit ec: ExecutionContext): TwitterStream =
    new TwitterStream(filter.toMap)

}