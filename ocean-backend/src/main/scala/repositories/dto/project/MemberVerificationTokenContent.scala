package org.abteilung6.ocean
package repositories.dto.project

import repositories.dto.BaseVerificationToken
import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }

case class MemberVerificationTokenContent(tokenType: String, memberId: Long) extends BaseVerificationToken {}

object MemberVerificationTokenContent {
  object Implicits {
    implicit val memberVerificationTokenContentDecoder: Decoder[MemberVerificationTokenContent] = deriveDecoder
    implicit val memberVerificationTokenContentEncoder: Encoder[MemberVerificationTokenContent] = deriveEncoder
  }
}
