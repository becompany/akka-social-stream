package ch.becompany.social.twitter

import com.typesafe.config.ConfigFactory

case class OAuthConfig(consumerKey: String, consumerSecret: String, accessToken: String, accessTokenSecret: String)

object OAuthConfig {

  private lazy val conf = ConfigFactory.load.getConfig("scalaSocialFeed.twitter")

  lazy val load: OAuthConfig = new OAuthConfig(
    conf.getString("consumerKey"),
    conf.getString("consumerSecret"),
    conf.getString("accessToken"),
    conf.getString("accessTokenSecret")
  )

}
