package ch.becompany.http.oauth

import com.typesafe.config.ConfigFactory

case class OAuthConfig(consumerKey: String, consumerSecret: String, accessToken: String, accessTokenSecret: String)

object OAuthConfig {

  def load(network: String): OAuthConfig = {
    val conf = ConfigFactory.load.getConfig(s"akkaSocialStream.$network")
      new OAuthConfig(
      conf.getString("consumerKey"),
      conf.getString("consumerSecret"),
      conf.getString("accessToken"),
      conf.getString("accessTokenSecret")
    )
  }

}
