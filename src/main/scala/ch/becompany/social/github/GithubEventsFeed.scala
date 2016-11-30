package ch.becompany.social.github

import java.time.Instant

import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Source
import ch.becompany.social.{SocialFeed, Status}

import scala.concurrent.ExecutionContext
import scala.util.Try

class GithubEventsFeed(org: String)(implicit ec: ExecutionContext) extends SocialFeed {

  val limit = 30

  override def source(numLast: Int): Source[Try[Status], _] = {
    Source.queue[Try[Status]](bufferSize = 1000, OverflowStrategy.dropTail)
        .mapMaterializedValue { queue =>
        GithubClient.events(org).foreach(_.take(numLast).foreach(status => {
          queue.offer(Try(status))
        }))
      }
  }
}
