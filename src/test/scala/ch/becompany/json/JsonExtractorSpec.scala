package ch.becompany.json

import org.scalatest._
import spray.json._

class JsonExtractorSpec extends FlatSpec with Matchers {

  "JSON extractor" should "extract JSON" in {

    import JsonExtractor._

    val obj = JsObject(
      "foo" -> JsString("foo value"),
      "ignore" -> JsString("ignore value"),
      "bar" -> JsString("bar value"),
      "question" -> JsObject(
        "answer" -> JsNumber(42)
      ),
      "baz" -> JsString("baz value"),
      "array" -> JsArray(
        JsString("e1"),
        JsString("e2")
      )
    )

    object extr extends JsonExtractor(
      EJsObject(
        "foo" -> JsString("foo value"),
        "bar" -> EJsString,
        "question" -> EJsObject(
          "answer" -> EJsNumber
        ),
        "baz" -> EJsString,
        "array" -> EJsArray(
          JsString("e1"),
          EJsString
        )
      )
    )

    extr.unapply(obj) shouldBe Some(Seq("bar value", 42, "baz value", "e2"))

  }

}
