package ch.becompany.social

import akka.NotUsed
import akka.stream.SourceShape
import akka.stream.scaladsl.{GraphDSL, Merge, Source}

import scala.util.Try

class Feed(feeds: SocialFeed*) {

  def stream(numLast: Int): Source[Try[Status], NotUsed] = {
/*
    val feeds = (feed1 :: feed2 :: rest.toList).toVector

    val last: Seq[Try[Status]] = feeds.
      map(_.last(numLast)).
      flatMap {
        case Success(statuses) => statuses.map(Success(_)): Seq[Try[Status]]
        case Failure(e) => Seq(Failure(e))
      }.
      sortWith {
        case (Success(s1), Success(s2)) => s1.date.isBefore(s2.date)
        case _ => false
      }.
      takeRight(numLast)
*/

    Source.fromGraph(GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val merge = builder.add(Merge[Try[Status]](feeds.size))
      feeds.
        map(_.stream(numLast)).
        zipWithIndex.
        foreach { case (src, i) => src ~> merge.in(i) }

      SourceShape(merge.out)
    })

  }

}
