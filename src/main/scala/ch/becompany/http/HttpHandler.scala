package ch.becompany.http

import java.io.IOException

import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, ResponseEntity}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}

trait HttpHandler[A] {
  def handle(request: HttpRequest, response: HttpResponse)(implicit ec: ExecutionContext): Future[A]
}

class UnmarshallingHttpHandler[A](implicit materializer: ActorMaterializer, unmarshaller: Unmarshaller[ResponseEntity, A])
  extends HttpHandler[A] with LazyLogging{

  def handle(request: HttpRequest, response: HttpResponse)(implicit ec: ExecutionContext): Future[A] =
    response.status match {
      case OK => Unmarshal(response.entity).to[A]
      case _ => handleError(response)
    }

  def handleError(response: HttpResponse)(implicit ec: ExecutionContext): Future[A] = {
    Unmarshal(response.entity).to[String].flatMap { entity =>
      val error = s"HTTP error ${response.status}: $entity"
      logger.error(error)
      Future.failed(new IOException(error))
    }
  }

}