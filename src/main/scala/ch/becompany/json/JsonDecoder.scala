package ch.becompany.json

import io.circe.{Decoder, Json}
import shapeless.{:+:, CNil, Coproduct, Generic}

trait CoproductJsonDecoder[A, C <: Coproduct] {
  def decode(json: Json): Option[A]
}

object CoproductJsonDecoder {

  implicit def decodeCNil[A]: CoproductJsonDecoder[A, CNil] =
    new CoproductJsonDecoder[A, CNil] {
      def decode(json: Json): Option[A] = None
    }

  implicit def decodeCCons[A, H <: A : Decoder, T <: Coproduct](
    implicit tailDecoder: CoproductJsonDecoder[A, T]
    ): CoproductJsonDecoder[A, H :+: T] =
    new CoproductJsonDecoder[A, H :+: T] {
      def decode(json: Json): Option[A] =
        json.as[H].right.toOption.orElse(tailDecoder.decode(json))
    }

}

trait JsonDecoder[A] {
  def extract(json: Json): Option[A]
}

object JsonDecoder {
  implicit def from[A, C <: Coproduct](
      implicit gen: Generic.Aux[A, C],
      extractor: CoproductJsonDecoder[A, C]
      ): JsonDecoder[A] =
    new JsonDecoder[A] {
      override def extract(json: Json): Option[A] =
        extractor.decode(json)
    }
}
