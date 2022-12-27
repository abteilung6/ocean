package org.abteilung6.ocean
package repositories.dto

import io.circe.{ Decoder, Encoder, HCursor, Json }
import sttp.tapir.Schema
import java.time.Instant

case class Account(
  accountId: Long,
  email: String,
  firstname: String,
  lastname: String,
  company: String,
  createdAt: Instant,
  authenticatorType: AuthenticatorType,
  verified: Boolean,
  passwordHash: Option[String]
)

object Account {
  object Implicits {

    implicit lazy val accountSchema: Schema[Account] = Schema.derived

    implicit val encodeAccount: Encoder[Account] = new Encoder[Account] {
      final def apply(a: Account): Json = Json.obj(
        ("accountId", Json.fromLong(a.accountId)),
        ("email", Json.fromString(a.email)),
        ("firstname", Json.fromString(a.firstname)),
        ("lastname", Json.fromString(a.lastname)),
        ("company", Json.fromString(a.company)),
        ("createdAt", Json.fromString(a.createdAt.toString)),
        ("authenticatorType", Json.fromString(a.authenticatorType.entryName)),
        ("verified", Json.fromBoolean(a.verified)),
        (
          "passwordHash",
          a.passwordHash match {
            case Some(value) => Json.fromString(value)
            case None        => Json.Null
          }
        )
      )
    }

    implicit val decodeAccount: Decoder[Account] = new Decoder[Account] {
      final def apply(c: HCursor): Decoder.Result[Account] =
        for {
          accountId <- c.downField("accountId").as[Long]
          email <- c.downField("email").as[String]
          firstname <- c.downField("firstname").as[String]
          lastname <- c.downField("lastname").as[String]
          company <- c.downField("company").as[String]
          createdAt <- c.downField("createdAt").as[Instant]
          authenticatorType <- c.downField("authenticatorType").as[AuthenticatorType]
          verified <- c.downField("verified").as[Boolean]
          passwordHash <- c.downField("passwordHash").as[Option[String]]
        } yield Account(
          accountId,
          email,
          firstname,
          lastname,
          company,
          createdAt,
          authenticatorType,
          verified,
          passwordHash
        )
    }
  }
}
