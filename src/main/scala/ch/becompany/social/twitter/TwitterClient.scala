package ch.becompany.social.twitter

import akka.http.scaladsl.model.{HttpRequest, Uri}
import ch.becompany.http.{CachingSupport, HttpClient, HttpUtils, UnmarshallingHttpHandler}
import ch.becompany.social.Status

import scala.concurrent.{ExecutionContext, Future}

class TwitterClient(screenName: String)
  extends HttpClient with CachingSupport with TwitterOAuthSupport with TwitterJsonSupport {

  import HttpUtils._

  val baseUrl = "https://api.twitter.com/1.1"

  implicit val handler = new UnmarshallingHttpHandler[List[Status]]()

  def latest(count: Int)(implicit ec: ExecutionContext): Future[List[Status]] = {
    val query = queryString("screen_name" -> screenName, "count" -> count.toString)
    req[List[Status]](HttpRequest(uri = Uri(s"$baseUrl/statuses/user_timeline.json?$query")))
  }

}
