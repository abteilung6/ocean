package org.abteilung6.ocean
package repositories.dto

import io.circe.{ Decoder, Encoder, HCursor, Json }
import sttp.tapir.Schema
import java.time.Instant

case class Account(
  id: Long,
  username: String,
  email: String,
  firstname: String,
  lastname: String,
  employeeType: String,
  createdAt: Instant,
  lastLoginAt: Option[Instant],
  expiresAt: Option[Instant]
)

object Account {
  object Implicits {

    implicit lazy val accountSchema: Schema[Account] = Schema.derived

    implicit val encodeAccount: Encoder[Account] = new Encoder[Account] {
      final def apply(a: Account): Json = Json.obj(
        ("id", Json.fromLong(a.id)),
        ("username", Json.fromString(a.username)),
        ("email", Json.fromString(a.email)),
        ("firstname", Json.fromString(a.firstname)),
        ("lastname", Json.fromString(a.lastname)),
        ("employeeType", Json.fromString(a.employeeType)),
        ("createdAt", Json.fromString(a.createdAt.toString)),
        (
          "lastLoginAt",
          a.lastLoginAt match {
            case Some(value) => Json.fromString(value.toString)
            case None        => Json.Null
          }
        ),
        (
          "expiresAt",
          a.expiresAt match {
            case Some(value) => Json.fromString(value.toString)
            case None        => Json.Null
          }
        )
      )
    }

    implicit val decodeAccount: Decoder[Account] = new Decoder[Account] {
      final def apply(c: HCursor): Decoder.Result[Account] =
        for {
          id <- c.downField("id").as[Long]
          username <- c.downField("username").as[String]
          email <- c.downField("email").as[String]
          firstname <- c.downField("firstname").as[String]
          lastname <- c.downField("lastname").as[String]
          employeeType <- c.downField("employeeType").as[String]
          createdAt <- c.downField("createdAt").as[Instant]
          lastLoginAt <- c.downField("lastLoginAt").as[Option[Instant]]
          expiresAt <- c.downField("expiresAt").as[Option[Instant]]
        } yield Account(
          id,
          username,
          email,
          firstname,
          lastname,
          employeeType,
          createdAt,
          lastLoginAt,
          expiresAt
        )
    }
  }
}
