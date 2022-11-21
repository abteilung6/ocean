package org.abteilung6.ocean
package repositories.dto.auth

import io.circe.{ Decoder, Encoder, HCursor, Json }

final case class RegisterAccountRequest(
  username: String,
  password: String,
  email: String,
  firstname: String,
  lastname: String
)

object RegisterAccountRequest {
  object Implicits {
    implicit val encodeRegisterAccountRequest: Encoder[RegisterAccountRequest] = new Encoder[RegisterAccountRequest] {
      final def apply(a: RegisterAccountRequest): Json = Json.obj(
        ("username", Json.fromString(a.username)),
        ("password", Json.fromString(a.password)),
        ("email", Json.fromString(a.email)),
        ("firstname", Json.fromString(a.firstname)),
        ("lastname", Json.fromString(a.lastname))
      )
    }

    implicit val decodeRegisterAccountRequest: Decoder[RegisterAccountRequest] = new Decoder[RegisterAccountRequest] {
      final def apply(c: HCursor): Decoder.Result[RegisterAccountRequest] =
        for {
          username <- c.downField("username").as[String]
          password <- c.downField("password").as[String]
          email <- c.downField("email").as[String]
          firstname <- c.downField("firstname").as[String]
          lastname <- c.downField("lastname").as[String]
        } yield RegisterAccountRequest(username, password, email, firstname, lastname)
    }
  }
}
