package ch.becompany.social.twitter

import ch.becompany.http.{CachingHttpClient, HttpUtils}
import ch.becompany.social.Status

import scala.concurrent.{ExecutionContext, Future}

class TwitterClient extends CachingHttpClient with TwitterJsonSupport {

  import HttpUtils._

  val baseUrl = "https://api.twitter.com/1.1"

  def latest(user: String, count: Int)(implicit ec: ExecutionContext): Future[List[Status]] = {
    val query = queryString("screen_name" -> user, "count" -> count.toString)
    req[List[Status]](s"$baseUrl/statuses/user_timeline.json?$query")
  }

}
