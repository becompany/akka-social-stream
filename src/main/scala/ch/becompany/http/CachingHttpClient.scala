package ch.becompany.http

import java.nio.file.{Files, Path, Paths}
import java.util.concurrent.TimeUnit

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.StatusCodes.{InternalServerError, NotModified, OK}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.scaladsl.FileIO

import scala.collection.immutable
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration

trait CachingHttpClient extends HttpClient {

  private val etagCache = new scala.collection.concurrent.TrieMap[String, String]()
  private val cacheDirectory = Files.createTempDirectory("github-cache")

  override def handle[A](uri: String, response: HttpResponse)(
    implicit unmarshaller: Unmarshaller[ResponseEntity, A], ec: ExecutionContext): Future[A] =
    response.status match {
      case OK => Unmarshal(addToCacheAndRead(uri, response)).to[A]
      case NotModified => Unmarshal(readFromCache(uri)).to[A]
      case _ => handleError(response)
    }

  override def getHeaders(uri: String) : immutable.Seq[HttpHeader] =
    etagCache.get(uri) match {
      case Some(etag) => scala.collection.immutable.Seq(HttpHeader.parse("If-None-Match", etag) match {
        case HttpHeader.ParsingResult.Ok(header, errors) => header
        case HttpHeader.ParsingResult.Error(_) => throw new IllegalStateException("Unable to create http header.")
      })
      case None => Nil
    }

  private def addToCacheAndRead(uri: String, httpResponse: HttpResponse): ResponseEntity = synchronized {
    httpResponse.headers.find(header => header.is("etag")) match {
      case Some(header) => {
        val etag = header.value().substring(1, header.value().length - 1)
        if ( etagCache.get(uri) match {
          case Some(cachedEtag) => cachedEtag != etag
          case None => true
        }) {
          val complete = httpResponse.entity.dataBytes.runWith(FileIO.toPath(getTempFilePath(etag)))
          Await.result(complete, Duration.create(2, TimeUnit.SECONDS))
          etagCache.put(uri, etag)
        }
      }
      case None => Unit
    }
    etagCache.get(uri) match {
      case Some(_) => readFromCache(uri)
      case None => httpResponse.entity
    }
  }

  private def readFromCache(uri: String): ResponseEntity = {
    etagCache.get(uri) match {
      case Some(etag) => HttpResponse(OK, entity = HttpEntity.fromPath(ContentType(MediaTypes.`application/json`), getTempFilePath(etag))).entity
      case None => HttpResponse(InternalServerError, entity = "").entity
    }
  }

  private def getTempFilePath(filename: String): Path =
    Paths.get(this.cacheDirectory.toString, filename)

}
