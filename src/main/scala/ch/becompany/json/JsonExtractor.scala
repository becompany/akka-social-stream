package ch.becompany.json

import spray.json._

class JsonExtractor(pattern: PJsValue) {

  def handle(value: JsValue, pattern: PJsValue): Option[Seq[Any]] =
    (value, pattern) match {
      case (a, Left(b)) if a == b => Some(Seq.empty)

      case (JsString(v), Right(EJsString)) => Some(Seq(v))
      case (JsNumber(v), Right(EJsNumber)) => Some(Seq(v))
      case (JsBoolean(v), Right(EJsBoolean)) => Some(Seq(v))
      case (obj: JsObject, Right(eObj: EJsObject)) =>
        val fields = eObj.fields.map { case (key, pValue) =>
          obj.getFields(key) match {
            case Seq(v) => handle(v, pValue)
            case _ => None
          }
        }
        if (fields.forall(_.isDefined))
          Some(fields.flatMap(_.get))
        else
          None
      case (arr: JsArray, Right(eArr: EJsArray)) =>
        val elements = arr.elements.
          zip(eArr.elements).
          map { case (v, pV) => handle(v, pV) }
        if (elements.size == arr.elements.size && elements.forall(_.isDefined))
          Some(elements.flatMap(_.get))
        else
          None
      case _ => None
    }

  def unapply(value: JsValue): Option[Seq[Any]] =
    handle(value, pattern)

}

object JsonExtractor {

  implicit def toPJsValue(v: JsValue): PJsValue = Left(v)

  implicit def toPJsValue(v: EJsValue): PJsValue = Right(v)

  implicit def jsFieldToPJsField(f: JsField): PJsField = (f._1, Left(f._2))

  implicit def eJsFieldToPJsField(f: EJsField): PJsField = (f._1, Right(f._2))

}

sealed abstract class EJsValue

case class EJsObject(fields: PJsField*) extends EJsValue

case class EJsArray(elements: PJsValue*) extends EJsValue

case object EJsString extends EJsValue
case object EJsNumber extends EJsValue
case object EJsBoolean extends EJsValue
