package org.abteilung6.ocean
package repositories.dto

import io.circe.{ Decoder, Encoder, HCursor, Json }
import sttp.tapir.Schema
import java.time.Instant

case class Account(
  accountId: Long,
  username: String,
  email: String,
  firstname: String,
  lastname: String,
  employeeType: String,
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
        ("username", Json.fromString(a.username)),
        ("email", Json.fromString(a.email)),
        ("firstname", Json.fromString(a.firstname)),
        ("lastname", Json.fromString(a.lastname)),
        ("employeeType", Json.fromString(a.employeeType)),
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
          username <- c.downField("username").as[String]
          email <- c.downField("email").as[String]
          firstname <- c.downField("firstname").as[String]
          lastname <- c.downField("lastname").as[String]
          employeeType <- c.downField("employeeType").as[String]
          createdAt <- c.downField("createdAt").as[Instant]
          authenticatorType <- c.downField("authenticatorType").as[AuthenticatorType]
          verified <- c.downField("verified").as[Boolean]
          passwordHash <- c.downField("passwordHash").as[Option[String]]
        } yield Account(
          accountId,
          username,
          email,
          firstname,
          lastname,
          employeeType,
          createdAt,
          authenticatorType,
          verified,
          passwordHash
        )
    }
  }
}
