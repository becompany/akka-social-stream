package ch.becompany.http

import java.io.IOException

import akka.http.scaladsl.model.StatusCodes.{NotModified, OK}
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.EntityTag
import akka.http.scaladsl.unmarshalling.Unmarshal

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}
import scalacache._
import scalacache.caffeine._

trait HttpCacheSupport extends HttpClient {

  type Cached[A] = (EntityTag, A)
  implicit val etagCache = ScalaCache(CaffeineCache())

  override def handle[A](handler: HttpHandler[A], request: HttpRequest, response: HttpResponse)
                        (implicit ec: ExecutionContext): Future[A] =
    response.status match {
      case OK =>
        for {
          body <- super.handle(handler, request, response)
          cached <- addToCache(request.uri, response, body)
        } yield body
      case NotModified => readFromCache(request.uri)
      case _ => super.handle(handler, request, response)
    }

  override def additionalHeaders(req: HttpRequest)(implicit ec: ExecutionContext): Future[immutable.Seq[HttpHeader]] =
    get[Cached[_], NoSerialization](req.uri) map {
      case Some((etag, _)) => scala.collection.immutable.Seq(headers.`If-None-Match`(etag))
      case None => Nil
    }

  private def addToCache[A](uri: Uri, response: HttpResponse, body: A)(implicit ec: ExecutionContext): Future[Unit] =
    response.header[headers.ETag].map(_.etag).map { etag =>
      put(uri)((etag, body))
    } getOrElse Future()

  private def addToCacheAndRead(uri: Uri, response: HttpResponse)(implicit ec: ExecutionContext): Future[ResponseEntity] =
    response.header[headers.ETag].map(_.etag).map { etag =>
      Unmarshal(response.entity).to[String].
        flatMap(content => put(uri)((etag, content))).
        flatMap(_ => readFromCache(uri))
    } getOrElse Future(response.entity)

  private def readFromCache[A](uri: Uri)(implicit ec: ExecutionContext): Future[A] = {
    get[Cached[A], NoSerialization](uri) flatMap {
      case Some((etag, content)) => Future(content)
      case None => Future.failed(new IOException("Cache entry not found"))
    }
  }

}
