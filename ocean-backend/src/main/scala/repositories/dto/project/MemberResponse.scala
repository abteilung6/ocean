package org.abteilung6.ocean
package repositories.dto.project

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
