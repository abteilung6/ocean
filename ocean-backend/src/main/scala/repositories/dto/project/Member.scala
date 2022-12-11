package org.abteilung6.ocean
package repositories.dto.project

import java.time.Instant

case class Member(
  memberId: Long,
  roleType: RoleType,
  state: MemberState,
  projectId: Long,
  accountId: Long,
  createdAt: Instant
)
