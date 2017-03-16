package ch.becompany.social.twitter

import java.time.Instant

import akka.http.scaladsl.model.{HttpRequest, Uri}
import ch.becompany.http.{CachingSupport, HttpClient, HttpUtils, UnmarshallingHttpHandler}
import ch.becompany.social.Status
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

/**
  * Client for the Twitter REST API.
  * @param screenName The screen name of the user, see
  *   [[https://dev.twitter.com/overview/api/users Twitter Developer Documentation]].
  */
class TwitterClient(screenName: String)
  extends HttpClient
    with CachingSupport
    with TwitterOAuthSupport
    with TwitterJsonSupport
    with LazyLogging {

  import HttpUtils._

  val baseUrl = "https://api.twitter.com/1.1"

  implicit val statusHandler = new UnmarshallingHttpHandler[List[(Instant, Status)]]()

  /**
    * Request the latest `count` tweets of the user, ordered by date in ascending order.
    * @param count The number of tweets to request.
    * @param ec The execution context.
    * @return A future list of tweets.
    */
  def latest(count: Int)(implicit ec: ExecutionContext): Future[List[(Instant, Status)]] = {
    logger.debug(s"""Requesting $count tweets for "$screenName"""")
    val query = queryString(
      "screen_name" -> screenName,
      "count" -> count.toString,
      "include_rts" -> true.toString
    )
    val uri = Uri(s"$baseUrl/statuses/user_timeline.json?$query")
    req[List[(Instant, Status)]](HttpRequest(uri = uri)) recover {
      case _ => List.empty
    }  map(_.reverse)
  }

  implicit val userIdHandler = new UnmarshallingHttpHandler[UserId]()

  /**
    * Request the user ID of the Twitter user.
    * @param ec The execution context.
    * @return A future string representation of the user ID.
    */
  def userId(implicit ec: ExecutionContext): Future[String] = {
    logger.debug(s"""Requesting user ID for "$screenName"""")
    val query = queryString("screen_name" -> screenName)
    req[UserId](HttpRequest(uri = Uri(s"$baseUrl/users/show.json?$query"))) recover {
      case _ => UserId("")
    } map(_.id)
  }

}
