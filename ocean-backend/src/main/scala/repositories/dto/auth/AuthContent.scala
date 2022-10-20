package org.abteilung6.ocean
package repositories.dto.auth

import io.circe.{ Encoder, Json }

sealed trait AuthContent

final case class AccessTokenContent(userId: Int) extends AuthContent

final case class RefreshTokenContent private (userId: Int) extends AuthContent

object AuthContent {
  object Implicits {

    implicit val encodeAccessTokenContent: Encoder[AccessTokenContent] = new Encoder[AccessTokenContent] {
      final def apply(a: AccessTokenContent): Json = Json.obj(
        ("userId", Json.fromInt(a.userId))
      )
    }

    implicit val encodeRefreshTokenContent: Encoder[RefreshTokenContent] = new Encoder[RefreshTokenContent] {
      final def apply(a: RefreshTokenContent): Json = Json.obj(
        ("userId", Json.fromInt(a.userId))
      )
    }
  }
}
