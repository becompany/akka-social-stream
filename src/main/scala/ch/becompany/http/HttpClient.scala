package ch.becompany.http

import java.io.IOException
import java.nio.file.{Files, Path, Paths}
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.{ActorMaterializer, IOResult}
import akka.stream.scaladsl._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Failure

trait HttpClient {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val etagCache = new scala.collection.concurrent.TrieMap[String, String]()
  val cacheDirectory = Files.createTempDirectory("github-cache")

  def req[A](uri: String)(implicit unmarshaller: Unmarshaller[ResponseEntity, A], ec: ExecutionContext): Future[A] =
    Http().singleRequest(HttpRequest(uri = uri, headers = getHeaders(uri))).
      flatMap { response =>
        response.status match {
          case OK => Unmarshal(addToCacheAndRead(uri, response)).to[A]
          case NotModified => Unmarshal(readFromCache(uri)).to[A]
          case _ => handleError(response)
        }
      }

  def handleError[A](response: HttpResponse)(implicit ec: ExecutionContext): Future[A] = {
    Unmarshal(response.entity).to[String].flatMap { entity =>
      val error = s"HTTP error ${response.status}: $entity"
      Future.failed(new IOException(error))
    }
  }

  private def getHeaders(uri: String) : scala.collection.immutable.Seq[HttpHeader] = etagCache.get(uri) match {
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

  private def getTempFilePath(filename: String): Path = Paths.get(this.cacheDirectory.toString, filename)
}
