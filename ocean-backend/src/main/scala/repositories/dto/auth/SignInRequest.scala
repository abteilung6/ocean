package org.abteilung6.ocean
package repositories.dto.auth

import io.circe.{ Decoder, Encoder, HCursor, Json }

final case class SignInRequest(email: String, password: String)

object SignInRequest {
  object Implicits {
    implicit val encodeSignInRequest: Encoder[SignInRequest] = new Encoder[SignInRequest] {
      final def apply(a: SignInRequest): Json = Json.obj(
        ("email", Json.fromString(a.email)),
        ("password", Json.fromString(a.password))
      )
    }

    implicit val decodeSignInRequest: Decoder[SignInRequest] = new Decoder[SignInRequest] {
      final def apply(c: HCursor): Decoder.Result[SignInRequest] =
        for {
          email <- c.downField("email").as[String]
          password <- c.downField("password").as[String]
        } yield new SignInRequest(email, password)
    }
  }
}
