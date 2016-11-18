package ch.becompany.social.twitter

import akka.stream.scaladsl.SourceQueueWithComplete
import ch.becompany.social.Status

import scala.util.{Failure, Success, Try}

trait QueueSupport {

  val queue: SourceQueueWithComplete[Try[Status]]

  implicit class LinkWithStatus(status: twitter4j.Status) {
    def getLink: String =
      s"https://twitter.com/${status.getUser.getScreenName}/status/${status.getId}"
  }

  def status(status: twitter4j.Status): Unit =
    queue.offer(Success(Status(
      status.getUser.getName,
      status.getCreatedAt.toInstant,
      status.getText,
      status.getLink)))

  def error(e: Exception): Unit =
    queue.offer(Failure(e))

}
