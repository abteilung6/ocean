package org.abteilung6.ocean
package repositories.dto.auth

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto._

final case class VerificationTokenContent(userId: Long)

object VerificationTokenContent {
  object Implicits {
    implicit val verificationTokenContentDecoder: Decoder[VerificationTokenContent] = deriveDecoder
    implicit val verificationTokenContentEncoder: Encoder[VerificationTokenContent] = deriveEncoder
  }
}
