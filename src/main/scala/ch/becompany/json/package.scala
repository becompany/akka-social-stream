package ch.becompany

import spray.json.JsValue

package object json {

  type PJsValue = Either[JsValue, EJsValue]

  type EJsField = (String, EJsValue)

  type PJsField = (String, PJsValue)

}
