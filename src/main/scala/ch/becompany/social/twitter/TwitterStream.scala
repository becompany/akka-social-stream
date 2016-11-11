package ch.becompany.social.twitter

import akka.stream.scaladsl.SourceQueueWithComplete
import ch.becompany.social.Status
import twitter4j.conf.Configuration
import twitter4j.{TwitterStreamFactory, UserStreamAdapter}

import scala.util.Try

class TwitterStreamListener(val queue: SourceQueueWithComplete[Try[Status]])
  extends UserStreamAdapter with QueueSupport {

  override def onStatus(st: twitter4j.Status): Unit = status(st)

  override def onException(e: Exception): Unit = error(e)

}

class TwitterStream(config: Configuration, user: Option[String]) {

  def stream(queue: SourceQueueWithComplete[Try[Status]]): Unit = {
    val twitterStream = new TwitterStreamFactory(config).getInstance
    val listener = new TwitterStreamListener(queue)
    twitterStream.addListener(listener)
    twitterStream.user()
  }

}