package org.abteilung6.ocean
package repositories.dto.auth

import io.circe.{ Decoder, Encoder, HCursor, Json }

final case class RegisterAccountRequest(
  email: String,
  password: String,
  firstname: String,
  lastname: String,
  company: String
)

object RegisterAccountRequest {
  object Implicits {
    implicit val encodeRegisterAccountRequest: Encoder[RegisterAccountRequest] = new Encoder[RegisterAccountRequest] {
      final def apply(a: RegisterAccountRequest): Json = Json.obj(
        ("email", Json.fromString(a.email)),
        ("password", Json.fromString(a.password)),
        ("firstname", Json.fromString(a.firstname)),
        ("lastname", Json.fromString(a.lastname)),
        ("company", Json.fromString(a.company))
      )
    }

    implicit val decodeRegisterAccountRequest: Decoder[RegisterAccountRequest] = new Decoder[RegisterAccountRequest] {
      final def apply(c: HCursor): Decoder.Result[RegisterAccountRequest] =
        for {
          password <- c.downField("password").as[String]
          email <- c.downField("email").as[String]
          firstname <- c.downField("firstname").as[String]
          lastname <- c.downField("lastname").as[String]
          company <- c.downField("company").as[String]
        } yield RegisterAccountRequest(email, password, firstname, lastname, company)
    }
  }
}
