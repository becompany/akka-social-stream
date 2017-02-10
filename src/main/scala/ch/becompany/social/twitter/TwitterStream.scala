package ch.becompany.social.twitter

import java.io.IOException
import java.time.Instant

import akka.http.scaladsl.model._
import akka.pattern.after
import akka.stream.{ActorAttributes, Supervision}
import akka.stream.scaladsl.{Flow, Framing, Source}
import akka.util.ByteString
import ch.becompany.http.{HttpClient, HttpHandler}
import ch.becompany.social.Status
import com.typesafe.scalalogging.LazyLogging
import spray.json._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

class TwitterStream(filter: Map[String, String], url: String, userUpdateInterval: FiniteDuration = 1 minute)
                   (implicit ec: ExecutionContext)
  extends HttpClient
    with TwitterOAuthSupport
    with TwitterJsonSupport
    with LazyLogging {

  import ActorAttributes._

  private val source = Uri(url)

  private val resumingDecider: Supervision.Decider = {
    case e: Exception =>
      logger.info(s"Encountered ${e.getClass}, resuming stream")
      Supervision.Resume
    case _ =>
      Supervision.Stop
  }

  // Twitter defines a limitation of 15 request per user. Resets every 15 minutes. => 1 req/m
  private val minUpdateInterval: FiniteDuration = 1 minute
  private val updateInterval = minUpdateInterval.max(userUpdateInterval)

  private def httpRequest(): HttpRequest = HttpRequest(
    method = HttpMethods.POST,
    uri = source,
    headers = List(headers.Accept(MediaRanges.`*/*`)),
    entity = FormData(filter).toEntity
  )

  private def streamResponse(response: HttpResponse): Future[Source[(Instant, Try[Status]), Any]] =
    Future(response.entity.dataBytes.
      via(Framing.delimiter(ByteString("\r\n"), maximumFrameLength = Int.MaxValue, allowTruncation = true)).
      map(_.utf8String).
      filter(_.length > 0).
      map { json =>
        val (date, status) = TweetJsonFormat.read(json.parseJson)
        (date, Try(status))
      }
    )

  private def message(response: HttpResponse): Future[String] =
    response.entity.toStrict(5 seconds).
      map(_.data.utf8String).
      map(msg => s"Response ${response.status}: $msg")

  private implicit object handler extends HttpHandler[Source[(Instant, Try[Status]), Any]] {
    override def handle(request: HttpRequest, response: HttpResponse)
                       (implicit ec: ExecutionContext): Future[Source[(Instant, Try[Status]), Any]] =
      response.status match {
        case StatusCodes.OK => streamResponse(response)
        case _ => message(response).flatMap(msg => Future.failed(new IOException(msg)))
      }
  }

  def stream: Source[(Instant, Try[Status]), Any] = {
    logger.debug(s"Streaming tweets with filter $filter")
    Source.
      repeat().
      via(
        Flow[Unit].
          mapAsync(1) { _ =>
            after(updateInterval, using = system.scheduler) {
              req(httpRequest())
            }
          }.
          withAttributes(supervisionStrategy(resumingDecider))
      ).
      flatMapConcat(identity)
  }

}

object TwitterStream {

  private val streamUrl = "https://stream.twitter.com/1.1/statuses/filter.json"

  def apply(filter: (String, String)*)(implicit ec: ExecutionContext): TwitterStream =
    new TwitterStream(filter.toMap, streamUrl)
  def apply(restartInterval: FiniteDuration, filter: (String, String)*)(implicit ec: ExecutionContext): TwitterStream =
    new TwitterStream(filter.toMap, streamUrl, restartInterval)

}