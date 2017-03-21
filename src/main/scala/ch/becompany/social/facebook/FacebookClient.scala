package ch.becompany.social.facebook

import java.time.Instant

import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import ch.becompany.http.{CachingSupport, HttpClient, UnmarshallingHttpHandler}
import ch.becompany.social.Status
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, Future}
import com.typesafe.scalalogging.LazyLogging

object FacebookClient extends HttpClient with FacebookJsonSupport with CachingSupport with LazyLogging {

  private val accessToken = ConfigFactory.load.getConfig("scalaSocialFeed.facebook")

  private val baseUrl = Uri("https://graph.facebook.com")
  private val graphVersion = "v2.8"

  private implicit val handler = new UnmarshallingHttpHandler[List[(Instant, Status)]]()


  private def getPageFeedUri(pageId: String): Uri = {
    Uri("/" + graphVersion + "/" + pageId + "/feed").resolvedAgainst(baseUrl).withQuery(Query.apply(
      "limit" -> s"${accessToken.getNumber("limit")}", "access_token" -> s"${accessToken.getString("appId")}|${accessToken.getString("appSecret")}"))
  }

  def posts(pageId: String)(implicit ec: ExecutionContext): Future[List[(Instant, Status)]] = {
    logger.debug("Requesting latest updates from Facebook page. [pageId={}]", pageId)
    val header = scala.collection.immutable.Seq(headers.`Accept`(MediaTypes.`application/json`))
    req[List[(Instant, Status)]](HttpRequest(uri = getPageFeedUri(pageId), headers = header))
      .recover {
        case e => logger.error("Error building the Facebook status.", e); List.empty
      }
  }
}
