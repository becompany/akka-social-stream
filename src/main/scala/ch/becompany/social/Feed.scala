package ch.becompany.social

import java.time.Instant

import akka.NotUsed
import akka.stream.scaladsl.{BroadcastHub, GraphDSL, Keep, Merge, Source}
import akka.stream.{Graph, Materializer, SourceShape}
import ch.becompany.util.PriorityBuffer

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

/**
  * Streams social network events.
  * @param feeds The social feeds to aggregate.
  * @param num The number of past events to prepend to the stream.
  */
class Feed[Tag](feeds: Map[Tag, SocialFeed], num: Int)
               (implicit ec: ExecutionContext, mat: Materializer) {

  private var latestBuf = PriorityBuffer.empty[StatusUpdate[Tag]](num)

  private def toStatusUpdate(tag: Tag): ((Instant, Status)) => StatusUpdate[Tag] =
    { case (date, status) => (tag, date, Try(status)) }

  private lazy val latest: Source[StatusUpdate[Tag], NotUsed] = {
    val taggedFeeds = feeds.map { case (tag, feed) =>
      feed.
        latest(num).
        map(_.map(toStatusUpdate(tag))).
        recover { case e => List((tag, Instant.now, Failure(e))) }
    }
    val f = Future.
        sequence(taggedFeeds).
        map(_.flatten.
          toList.
          sortBy(_._2).
          takeRight(num))

    Source.
      fromFuture(f).
      flatMapConcat(list => Source.fromIterator(() => list.iterator))
  }

  private lazy val streamFeeds: Graph[SourceShape[StatusUpdate[Tag]], NotUsed] =
    GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val merge = builder.add(Merge[StatusUpdate[Tag]](feeds.size))
      feeds.
        map { case (tag, feed) =>
          feed.stream().map { case (date, status) =>
            (tag, date, status)
          }
        }.
        zipWithIndex.
        foreach { case (src, i) => src ~> merge.in(i) }

      SourceShape(merge.out)
    }

  private lazy val stream: Source[StatusUpdate[Tag], NotUsed] =
    latest.concat(streamFeeds)

  private lazy val producer: Source[StatusUpdate[Tag], NotUsed] =
    stream.
      map(s => { latestBuf = latestBuf + s; s }).
      toMat(BroadcastHub.sink(bufferSize = 256))(Keep.right).
      run()

  def subscribe: Source[StatusUpdate[Tag], NotUsed] =
    Source(latestBuf).concat(producer)

}

object Feed {

  def apply[Tag](feeds: (Tag, SocialFeed)*)(num: Int)
                (implicit ec: ExecutionContext, mat: Materializer): Feed[Tag] =
    new Feed(feeds.toMap, num)
}