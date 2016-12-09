package ch.becompany.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer

import scala.concurrent.{ExecutionContext, Future}

abstract class HttpClient {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  def req[A](req: HttpRequest)(implicit handler: HttpHandler[A], ec: ExecutionContext): Future[A] =
    for {
      headers <- additionalHeaders(req)
      request = req.withHeaders(req.headers ++ headers)
      response <- Http().singleRequest(request)
      result <- handle(handler, request, response)
    } yield result

  def handle[A](handler: HttpHandler[A], request: HttpRequest, response: HttpResponse)
               (implicit ec: ExecutionContext): Future[A] =
    handler.handle(request, response)

  def additionalHeaders(req: HttpRequest)(implicit ec: ExecutionContext): Future[Seq[HttpHeader]] =
    Future(Seq.empty)

}
