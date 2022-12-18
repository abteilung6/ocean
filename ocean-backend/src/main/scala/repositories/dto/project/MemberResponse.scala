package org.abteilung6.ocean
package repositories.dto.project

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import repositories.dto.AuthenticatorType
import java.time.Instant

case class MemberResponse(
  /**
   * Member
   */
  memberId: Long,
  roleType: RoleType,
  state: MemberState,
  createdAt: Instant,

  /**
   * Account
   */
  accountId: Long,
  accountUsername: String,
  accountAuthenticatorType: AuthenticatorType,
  accountEmail: String,

  /**
   * Project
   */
  projectId: Long,
  projectName: String
)

object MemberResponse {
  object Implicits {
    implicit val memberResponseDecoder: Decoder[MemberResponse] = deriveDecoder
    implicit val memberResponseEncoder: Encoder[MemberResponse] = deriveEncoder
  }
}
