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

object Member {
  def fromCreateMemberRequest(createMemberRequest: CreateMemberRequest, memberState: MemberState): Member =
    Member(
      0L,
      createMemberRequest.roleType,
      memberState,
      createMemberRequest.projectId,
      createMemberRequest.accountId,
      Instant.now()
    )
}
