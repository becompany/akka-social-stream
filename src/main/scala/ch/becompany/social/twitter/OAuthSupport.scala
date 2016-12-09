package ch.becompany.social.twitter

import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.GenericHttpCredentials
import akka.http.scaladsl.model.{HttpHeader, headers}
import com.hunorkovacs.koauth.domain.KoauthRequest
import com.hunorkovacs.koauth.service.consumer.DefaultConsumerService

import scala.concurrent.{ExecutionContext, Future}

trait OAuthSupport {

  import ch.becompany.http.HttpUtils._

  val system: ActorSystem

  private val oauthHeaderName = "OAuth"

  private val conf = OAuthConfig.load

  private lazy val consumer = new DefaultConsumerService(system.dispatcher)

  def oauthHeader(url: String, body: Map[String, String])(implicit ex: ExecutionContext): Future[HttpHeader] =
    consumer.createOauthenticatedRequest(
      KoauthRequest(
        method = "POST",
        url = url,
        authorizationHeader = None,
        body = Some(queryString(body))),
      conf.consumerKey, conf.consumerSecret, conf.accessToken, conf.accessTokenSecret).
      map(_.header).
      map(header => headers.Authorization(GenericHttpCredentials(
        oauthHeaderName, header.substring(oauthHeaderName.length + 1))))

}
