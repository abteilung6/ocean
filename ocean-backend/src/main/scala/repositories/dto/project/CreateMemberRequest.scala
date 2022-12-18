package org.abteilung6.ocean
package repositories.dto.project

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }

case class CreateMemberRequest(
  projectId: Long,
  accountId: Long,
  roleType: RoleType
)

object CreateMemberRequest {
  object Implicits {
    implicit val createMemberRequestDecoder: Decoder[CreateMemberRequest] = deriveDecoder
    implicit val createMemberRequestEncoder: Encoder[CreateMemberRequest] = deriveEncoder
  }
}
