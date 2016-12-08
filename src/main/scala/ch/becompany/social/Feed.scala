package ch.becompany.social

import akka.NotUsed
import akka.stream.SourceShape
import akka.stream.scaladsl.{GraphDSL, Merge, Source}

import scala.util.Try

class Feed[Tag](feeds: Map[Tag, SocialFeed]) {

  def source(numLast: Int): Source[(Tag, Try[Status]), NotUsed] =
    Source.fromGraph(GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val merge = builder.add(Merge[(Tag, Try[Status])](feeds.size))
      feeds.
        map { case (tag, feed) => feed.source(numLast).map((tag, _)) }.
        zipWithIndex.
        foreach { case (src, i) => src ~> merge.in(i) }

      SourceShape(merge.out)
    })

}

object Feed {
  def apply[Tag](feeds: (Tag, SocialFeed)*): Feed[Tag] =
    new Feed(feeds.toMap)
}