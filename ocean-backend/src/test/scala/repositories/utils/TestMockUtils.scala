package org.abteilung6.ocean
package repositories.utils

import repositories.dto.{ Account, AuthenticatorType }
import repositories.dto.auth.RegisterAccountRequest
import repositories.dto.project.{ CreateMemberRequest, Member, MemberResponse, MemberState, Project, RoleType }

import java.time.Instant

object TestMockUtils {

  def getMockAccount(
    accountId: Long = 0L,
    username: String = "username1",
    email: String = "username1@localhost.com",
    firstname: String = "firstname1",
    lastname: String = "lastname2",
    employeeType: String = "student",
    createdAt: Instant = Instant.now(),
    authenticatorType: AuthenticatorType = AuthenticatorType.Directory,
    verified: Boolean = false,
    passwordHash: Option[String] = None
  ): Account =
    Account(
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

  def getMockRegisterAccountRequest(
    username: String = "username",
    password: String = "password",
    email: String = "alice@bob.com",
    firstname: String = "Firstname",
    lastname: String = "Lastname"
  ): RegisterAccountRequest =
    RegisterAccountRequest(username, password, email, firstname, lastname)

  def getMockProject(
    projectId: Long = 0L,
    name: String = "my-project-1",
    description: String = "Short description",
    createdAt: Instant = Instant.now(),
    ownerId: Long = 0L
  ): Project =
    Project(projectId, name, description, createdAt, ownerId)

  def getMockMember(
    memberId: Long = 0L,
    roleType: RoleType = RoleType.Developer,
    state: MemberState = MemberState.Active,
    projectId: Long = 0L,
    accountId: Long = 0L,
    createdAt: Instant = Instant.now()
  ): Member = Member(memberId, roleType, state, projectId, accountId, createdAt)

  def getMockMemberResponse(
    memberId: Long = 0L,
    roleType: RoleType = RoleType.Developer,
    state: MemberState = MemberState.Active,
    createdAt: Instant = Instant.now(),
    accountId: Long = 0L,
    accountUsername: String = "username",
    accountAuthenticatorType: AuthenticatorType = AuthenticatorType.Credentials,
    accountEmail: String = "alice@bob.com",
    projectId: Long = 0L,
    projectName: String = "my-project-1"
  ): MemberResponse =
    MemberResponse(
      memberId,
      roleType,
      state,
      createdAt,
      accountId,
      accountUsername,
      accountAuthenticatorType,
      accountEmail,
      projectId,
      projectName
    )

  def getMockCreateMemberRequest(
    projectId: Long = 0L,
    accountId: Long = 0L,
    roleType: RoleType = RoleType.Developer
  ): CreateMemberRequest =
    CreateMemberRequest(projectId, accountId, roleType)
}
