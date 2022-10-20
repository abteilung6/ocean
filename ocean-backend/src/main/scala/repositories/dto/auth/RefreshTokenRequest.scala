package org.abteilung6.ocean
package repositories.dto.auth

import io.circe.{ Decoder, Encoder, HCursor, Json }

final case class RefreshTokenRequest(refreshToken: String)

object RefreshTokenRequest {
  object Implicits {
    implicit val encodeRefreshTokenRequest: Encoder[RefreshTokenRequest] = new Encoder[RefreshTokenRequest] {
      final def apply(a: RefreshTokenRequest): Json = Json.obj(
        ("refreshToken", Json.fromString(a.refreshToken))
      )
    }

    implicit val decodeRefreshTokenRequest: Decoder[RefreshTokenRequest] = new Decoder[RefreshTokenRequest] {
      final def apply(c: HCursor): Decoder.Result[RefreshTokenRequest] =
        for {
          refreshToken <- c.downField("refreshToken").as[String]
        } yield new RefreshTokenRequest(refreshToken)
    }
  }
}
