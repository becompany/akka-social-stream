package ch.becompany.util

import scala.collection.immutable.Iterable

class PriorityBuffer[A](val elements: Vector[A], max: Int)(implicit ordering: Ordering[A])
  extends Iterable[A] {

  def +(elem: A): PriorityBuffer[A] =
    new PriorityBuffer((elements :+ elem).sorted.takeRight(max), max)

  override def iterator: Iterator[A] =
    elements.iterator
}

object PriorityBuffer {

  def empty[A](max: Int)(implicit ordering: Ordering[A]): PriorityBuffer[A] =
    new PriorityBuffer(Vector.empty, max)

}