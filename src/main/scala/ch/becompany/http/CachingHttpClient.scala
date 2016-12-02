package ch.becompany.http

import akka.http.scaladsl.model.StatusCodes.{InternalServerError, NotModified, OK}
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.EntityTag
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}
import scalacache._
import scalacache.caffeine._

trait CachingHttpClient extends HttpClient {

  type Cached = (EntityTag, String)
  implicit val etagCache = ScalaCache(CaffeineCache())

  override def handle[A](uri: String, response: HttpResponse)(
    implicit unmarshaller: Unmarshaller[ResponseEntity, A], ec: ExecutionContext): Future[A] =
    response.status match {
      case OK => addToCacheAndRead(uri, response).flatMap(Unmarshal(_).to[A])
      case NotModified => readFromCache(uri).flatMap(Unmarshal(_).to[A])
      case _ => handleError(response)
    }

  override def getHeaders(uri: String)(implicit ec: ExecutionContext): Future[immutable.Seq[HttpHeader]] =
    get[Cached, NoSerialization](uri) map {
      case Some((etag, _)) => scala.collection.immutable.Seq(headers.`If-None-Match`(etag))
      case None => Nil
    }

  private def addToCacheAndRead(uri: String, response: HttpResponse)(implicit ec: ExecutionContext): Future[ResponseEntity] =
    response.header[headers.ETag].map(_.etag).map { etag =>
      Unmarshal(response.entity).to[String].
        flatMap(content => put(uri)((etag, content))).
        flatMap(_ => readFromCache(uri))
    } getOrElse Future(response.entity)

  private def readFromCache(uri: String)(implicit ec: ExecutionContext): Future[ResponseEntity] = {
    get[Cached, NoSerialization](uri) map {
      case Some((etag, content)) =>
        HttpResponse(OK, entity = HttpEntity(ContentType(MediaTypes.`application/json`), content)).entity
      case None =>
        HttpResponse(InternalServerError, entity = "").entity
    }
  }

}
