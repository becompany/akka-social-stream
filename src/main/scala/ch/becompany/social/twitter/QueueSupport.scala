package ch.becompany.social.twitter

import akka.stream.scaladsl.SourceQueueWithComplete
import ch.becompany.social.Status

import scala.util.{Failure, Success, Try}

trait QueueSupport {

  val queue: SourceQueueWithComplete[Try[Status]]

  def status(status: twitter4j.Status): Unit =
    queue.offer(Success(Status(
      status.getUser.getName,
      status.getCreatedAt.toInstant,
      status.getText)))

  def error(e: Exception): Unit =
    queue.offer(Failure(e))

}
