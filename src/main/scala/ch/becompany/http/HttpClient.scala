package ch.becompany.http

import java.io.IOException

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.ActorMaterializer

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

trait HttpClient {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  def req[A](uri: String)(implicit unmarshaller: Unmarshaller[ResponseEntity, A], ec: ExecutionContext): Future[A] =
    getHeaders(uri).flatMap { headers =>
      Http().
        singleRequest(HttpRequest(uri = uri, headers = headers)).
        flatMap(response => handle(uri, response))
    }

  def handle[A](uri: String, response: HttpResponse)(
    implicit unmarshaller: Unmarshaller[ResponseEntity, A], ec: ExecutionContext): Future[A] =
    response.status match {
      case OK => Unmarshal(response.entity).to[A]
      case _ => handleError(response)
    }

  def handleError[A](response: HttpResponse)(implicit ec: ExecutionContext): Future[A] = {
    Unmarshal(response.entity).to[String].flatMap { entity =>
      val error = s"HTTP error ${response.status}: $entity"
      Future.failed(new IOException(error))
    }
  }

  def getHeaders(uri: String)(implicit ec: ExecutionContext): Future[immutable.Seq[HttpHeader]] =
    Future(immutable.Seq.empty)

}
