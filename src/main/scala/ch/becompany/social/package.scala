package ch.becompany

import java.time.Instant

import scala.util.Try

package object social {

  type StatusUpdate[Tag] = (Tag, Instant, Try[Status])

  implicit def statusUpdateOrdering[T] = Ordering.by[StatusUpdate[T], Instant](_._2)

}
