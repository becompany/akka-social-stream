package ch.becompany.http.oauth

import akka.http.scaladsl.model.headers.GenericHttpCredentials
import akka.http.scaladsl.model.{HttpHeader, HttpRequest, headers}
import akka.http.scaladsl.unmarshalling.Unmarshal
import ch.becompany.http.HttpClient
import com.hunorkovacs.koauth.domain.KoauthRequest
import com.hunorkovacs.koauth.service.consumer.DefaultConsumerService

import scala.concurrent.{ExecutionContext, Future}

trait OAuthSupport extends HttpClient {

  private val oauthHeaderName = "OAuth"

  val oauthConfig: OAuthConfig

  private lazy val consumer = new DefaultConsumerService(system.dispatcher)

  def oauthHeader(request: HttpRequest)(implicit ex: ExecutionContext): Future[HttpHeader] =
    Unmarshal(request.entity).to[String].flatMap { body =>
      consumer.createOauthenticatedRequest(
        KoauthRequest(
          method = request.method.value,
          url = request.uri.toString,
          authorizationHeader = None,
          body = Some(body)
        ),
        oauthConfig.consumerKey,
        oauthConfig.consumerSecret,
        oauthConfig.accessToken,
        oauthConfig.accessTokenSecret
      ).
        map(_.header).
        map(header => headers.Authorization(GenericHttpCredentials(
          oauthHeaderName, header.substring(oauthHeaderName.length + 1))))
    }

  abstract override def additionalHeaders(request: HttpRequest)(implicit ec: ExecutionContext): Future[Seq[HttpHeader]] = {
    val headersFuture = super.additionalHeaders(request)
    val oauthHeaderFuture = oauthHeader(request)
    for (headers <- headersFuture; oauthHeader <- oauthHeaderFuture)
      yield headers :+ oauthHeader
  }

}
