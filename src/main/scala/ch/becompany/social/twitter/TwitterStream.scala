package ch.becompany.social.twitter

import java.io.IOException

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Framing, Source}
import akka.util.ByteString
import ch.becompany.social.Status
import org.json4s.DefaultFormats
import spray.json._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

class TwitterStream(filter: Map[String, String])(implicit ec: ExecutionContext)
  extends OAuthSupport with TwitterJsonSupport {

  private val url = "https://stream.twitter.com/1.1/statuses/filter.json"

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val formats = DefaultFormats

  private val source = Uri(url)

  private def httpRequest(oauthHeader: HttpHeader): HttpRequest =  HttpRequest(
    method = HttpMethods.POST,
    uri = source,
    headers = List(oauthHeader, headers.Accept(MediaRanges.`*/*`)),
    entity = FormData(filter).toEntity
  )

  private def httpSuccessSource(response: HttpResponse): Future[Source[Try[Status], Any]] =
    Future(response.entity.dataBytes.
      via(Framing.delimiter(ByteString("\r\n"), maximumFrameLength = Int.MaxValue, allowTruncation = true)).
      map(_.utf8String).
      map(json => Try(TweetJsonFormat.read(json.parseJson))))

  private def httpErrorSource(response: HttpResponse): Future[Source[Try[Status], Any]] =
    response.entity.toStrict(5 seconds).
      map(_.data.utf8String).
      map(msg => Source.single(Failure(new IOException(msg))))

  private def httpSource(response: HttpResponse): Future[Source[Try[Status], Any]] =
    response.status match {
      case StatusCodes.OK => httpSuccessSource(response)
      case _ => httpErrorSource(response)
    }

  private def request: Future[Source[Try[Status], Any]] =
    for {
      oauthHdr <- oauthHeader(url, filter)
      response <- Http().singleRequest(httpRequest(oauthHdr))
      source <- httpSource(response)
    } yield {
      source
    }

  def stream: Source[Try[Status], Any] = {
    // Emit error instead of failing the complete stream
    val futureSrc = request.recover { case e => Source.single(Failure(e)) }
    Source.fromFuture(futureSrc).flatMapConcat(identity)
  }

}
