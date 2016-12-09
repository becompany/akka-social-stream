package ch.becompany.social.twitter

import ch.becompany.http.oauth.OAuthConfig

trait TwitterOAuthConfig {

  private val oauthConfig = OAuthConfig.load("twitter")

}
