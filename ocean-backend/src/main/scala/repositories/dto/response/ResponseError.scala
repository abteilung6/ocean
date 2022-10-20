package org.abteilung6.ocean
package repositories.dto.response

import io.circe.{ Decoder, Encoder, HCursor, Json }

case class ResponseError(message: String)

object ResponseError {
  object Implicits {
    implicit val encodeResponseError: Encoder[ResponseError] = new Encoder[ResponseError] {
      final def apply(a: ResponseError): Json = Json.obj(
        ("message", Json.fromString(a.message))
      )
    }

    implicit val decodeResponseError: Decoder[ResponseError] = new Decoder[ResponseError] {
      final def apply(c: HCursor): Decoder.Result[ResponseError] =
        for {
          message <- c.downField("message").as[String]
        } yield new ResponseError(message)
    }
  }
}
