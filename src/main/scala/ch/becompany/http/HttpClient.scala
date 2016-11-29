package ch.becompany.http

import java.io.{File, IOException}
import java.nio.file.{Files, Path, Paths}

import akka.actor.ActorSystem
import akka.{Done, stream}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpHeader, HttpRequest, HttpResponse, ResponseEntity}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Sink}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

trait HttpClient {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val etagCache = new scala.collection.concurrent.TrieMap[String, String]()
  val cacheDirectory = Files.createTempDirectory("github-cache")

  def req[A](uri: String, headers: scala.collection.immutable.Seq[HttpHeader])(implicit unmarshaller: Unmarshaller[ResponseEntity, A], ec: ExecutionContext): Future[A] =
    Http().singleRequest(HttpRequest(uri = uri)).
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

  private def addToCacheAndRead(uri: String, httpResponse: HttpResponse): ResponseEntity = {
    httpResponse.headers.find(header => header.is("etag")) match {
      case Some(header) => {
        etagCache.put(uri, header.value())

        httpResponse.entity.dataBytes.map(_).toMat(FileIO.toPath(getTempFilePath(header.value())))
      }
      case None => Unit
    }
    etagCache.get(uri) match {
      case Some(etag) => readFromCache(etag)
      case None => httpResponse.entity
    }
  }

  private def readFromCache(uri: String): ResponseEntity = {
    etagCache.get(uri) match {
      case Some(etag) => FileIO.fromPath(getTempFilePath(etag)).to()
    }
  }

  private def getTempFilePath(filename: String): Path = Paths.get(this.cacheDirectory.toString, filename)
}
