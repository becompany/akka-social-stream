package ch.becompany.json

import cats.syntax.either._
import io.circe.{Decoder, Encoder}

object Codecs {

  import java.time.Instant

  implicit val encodeInstant: Encoder[Instant] =
    Encoder.encodeString.contramap[Instant](_.toString)

  implicit val decodeInstant: Decoder[Instant] =
    Decoder.decodeString.emap { str =>
      Either.catchNonFatal(Instant.parse(str)).leftMap(t => s"Instant $str")
    }

}
