package ch.becompany.social.twitter

import akka.http.scaladsl.model.{HttpRequest, Uri}
import ch.becompany.http.{CachingSupport, HttpClient, HttpUtils, UnmarshallingHttpHandler}
import ch.becompany.social.Status

import scala.concurrent.{ExecutionContext, Future}

/**
  * Client for the Twitter REST API.
  * @param screenName The screen name of the user, see
  *   [[https://dev.twitter.com/overview/api/users Twitter Developer Documentation]].
  */
class TwitterClient(screenName: String)
  extends HttpClient with CachingSupport with TwitterOAuthSupport with TwitterJsonSupport {

  import HttpUtils._

  val baseUrl = "https://api.twitter.com/1.1"

  implicit val statusHandler = new UnmarshallingHttpHandler[List[Status]]()

  /**
    * Request the latest `count` tweets of the user.
    * @param count The number of tweets to request.
    * @param ec The execution context.
    * @return A future list of tweets.
    */
  def latest(count: Int)(implicit ec: ExecutionContext): Future[List[Status]] = {
    val query = queryString("screen_name" -> screenName, "count" -> count.toString)
    req[List[Status]](HttpRequest(uri = Uri(s"$baseUrl/statuses/user_timeline.json?$query")))
  }

  implicit val userIdHandler = new UnmarshallingHttpHandler[UserId]()

  /**
    * Request the user ID of the Twitter user.
    * @param ec The execution context.
    * @return A future string representation of the user ID.
    */
  def userId(implicit ec: ExecutionContext): Future[String] = {
    val query = queryString("screen_name" -> screenName)
    req[UserId](HttpRequest(uri = Uri(s"$baseUrl/users/show.json?$query"))).map(_.id)
  }

}
