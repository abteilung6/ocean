package org.abteilung6.ocean
package repositories.dto.response

import io.circe.{ Decoder, Encoder, HCursor, Json }

case class ResponseError(statusCode: Int, message: String)

object ResponseError {
  object Implicits {
    implicit val encodeResponseError: Encoder[ResponseError] = new Encoder[ResponseError] {
      final def apply(a: ResponseError): Json = Json.obj(
        ("statusCode", Json.fromInt(a.statusCode)),
        ("message", Json.fromString(a.message))
      )
    }

    implicit val decodeResponseError: Decoder[ResponseError] = new Decoder[ResponseError] {
      final def apply(c: HCursor): Decoder.Result[ResponseError] =
        for {
          statusCode <- c.downField("statusCode").as[Int]
          message <- c.downField("message").as[String]
        } yield new ResponseError(statusCode, message)
    }
  }
}
