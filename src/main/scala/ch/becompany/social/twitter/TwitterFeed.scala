package ch.becompany.social.twitter

import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Source
import ch.becompany.social.{SocialFeed, Status}
import com.typesafe.config.ConfigFactory
import twitter4j.conf.ConfigurationBuilder

import scala.util.Try

class TwitterFeed(user: Option[String]) extends SocialFeed {

  private lazy val conf = ConfigFactory.load.getConfig("scalaSocialFeed.twitter")

  private lazy val config = new ConfigurationBuilder().
    setOAuthConsumerKey(conf.getString("consumerKey")).
    setOAuthConsumerSecret(conf.getString("consumerSecret")).
    setOAuthAccessToken(conf.getString("accessToken")).
    setOAuthAccessTokenSecret(conf.getString("accessTokenSecret")).
    build

  def source(num: Int): Source[Try[Status], _] = {

    val async = new TwitterAsync(config, user)
    val stream = new TwitterStream(config, user)

    Source.queue[Try[Status]](bufferSize = 1000, OverflowStrategy.fail).
      mapMaterializedValue { queue =>
        async.recent(num)(queue)
        stream.stream(queue)
      }
  }

}
