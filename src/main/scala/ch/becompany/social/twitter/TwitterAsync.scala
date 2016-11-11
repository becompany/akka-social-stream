package ch.becompany.social.twitter

import akka.stream.scaladsl.SourceQueueWithComplete
import ch.becompany.social.Status
import twitter4j._
import twitter4j.conf.Configuration

import scala.collection.JavaConversions._
import scala.util.Try

class TwitterListener(val queue: SourceQueueWithComplete[Try[Status]])
  extends TwitterAdapter with QueueSupport {

  override def gotUserTimeline(statuses: ResponseList[twitter4j.Status]): Unit =
    statuses.iterator.foreach(status)

  override def onException(e: TwitterException, method: TwitterMethod): Unit =
    error(e)
}

class TwitterAsync(config: Configuration, user: Option[String]) {

  private val paging = new Paging(1, 5)

  def recent(num: Int)(queue: SourceQueueWithComplete[Try[Status]]): Unit = {
    val asyncTwitter = new AsyncTwitterFactory(config).getInstance
    val listener = new TwitterListener(queue)
    asyncTwitter.addListener(listener)

    user match {
      case Some(screenName) => asyncTwitter.getUserTimeline(screenName, paging)
      case None => asyncTwitter.getUserTimeline(paging)
    }
  }


}