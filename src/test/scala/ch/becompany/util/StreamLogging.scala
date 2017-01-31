package ch.becompany.util

import akka.NotUsed
import akka.stream.scaladsl.Flow
import com.typesafe.scalalogging.LazyLogging

object StreamLogging extends LazyLogging {

  def logElements[A]: Flow[A, A, NotUsed] =
    Flow[A].map(a => { logger.info(a.toString); a })

}
