package org.abteilung6.ocean
package repositories.dto.auth

import io.circe.{ Decoder, Encoder, HCursor, Json }

final case class AuthResponse(accessToken: String, refreshToken: String)

object AuthResponse {
  object Implicits {
    implicit val encodeAuthResponse: Encoder[AuthResponse] = new Encoder[AuthResponse] {
      final def apply(a: AuthResponse): Json = Json.obj(
        ("accessToken", Json.fromString(a.accessToken)),
        ("refreshToken", Json.fromString(a.refreshToken))
      )
    }

    implicit val decodeAuthResponse: Decoder[AuthResponse] = new Decoder[AuthResponse] {
      final def apply(c: HCursor): Decoder.Result[AuthResponse] =
        for {
          accessToken <- c.downField("accessToken").as[String]
          refreshToken <- c.downField("refreshToken").as[String]
        } yield new AuthResponse(accessToken, refreshToken)
    }
  }
}
