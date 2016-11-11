package ch.becompany.social.twitter

import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Source
import ch.becompany.social.{SocialFeed, Status}
import twitter4j.conf.ConfigurationBuilder

import scala.util.Try

case class TwitterAuthConfig(
  consumerKey: String,
  consumerSecret: String,
  accessToken: String,
  accessTokenSecret: String)

class TwitterFeed(auth: TwitterAuthConfig, user: Option[String]) extends SocialFeed {

  private lazy val config = new ConfigurationBuilder().
    setOAuthConsumerKey(auth.consumerKey).
    setOAuthConsumerSecret(auth.consumerSecret).
    setOAuthAccessToken(auth.accessToken).
    setOAuthAccessTokenSecret(auth.accessTokenSecret).
    build

  def stream(num: Int): Source[Try[Status], _] = {

    val async = new TwitterAsync(config, user)
    val stream = new TwitterStream(config, user)

    Source.queue[Try[Status]](bufferSize = 1000, OverflowStrategy.fail).
      mapMaterializedValue { queue =>
        async.recent(num)(queue)
        stream.stream(queue)
      }
  }

}
