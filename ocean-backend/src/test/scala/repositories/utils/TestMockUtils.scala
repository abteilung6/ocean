package org.abteilung6.ocean
package repositories.utils

import repositories.dto.{ Account, AuthenticatorType }
import repositories.dto.auth.RegisterAccountRequest
import repositories.dto.project.Project
import java.time.Instant

object TestMockUtils {

  def getMockAccount(
    accountId: Long = 0L,
    username: String = "username1",
    email: String = "username1@localhost",
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
}
