package ch.becompany.social.twitter

import ch.becompany.http.oauth.{OAuthConfig, OAuthSupport}

trait TwitterOAuthSupport extends OAuthSupport {

  val oauthConfig: OAuthConfig = OAuthConfig.load("twitter")

}
