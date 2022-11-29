package org.abteilung6.ocean
package repositories.utils

import repositories.dto.{ Account, AuthenticatorType }
import repositories.dto.auth.RegisterAccountRequest
import java.time.Instant

object TestMockUtils {

  def getMockAccount(
    id: Long = 0L,
    username: String = "username1",
    email: String = "username1@localhost",
    firstname: String = "firstname1",
    lastname: String = "lastname2",
    employeeType: String = "student",
    createdAt: Instant = Instant.now(),
    authenticatorType: AuthenticatorType = AuthenticatorType.Directory,
    verified: Boolean = false
  ): Account =
    Account(
      id,
      username,
      email,
      firstname,
      lastname,
      employeeType,
      createdAt,
      authenticatorType,
      verified
    )

  def getMockRegisterAccountRequest(
    username: String = "username",
    password: String = "password",
    email: String = "alice@bob.com",
    firstname: String = "Firstname",
    lastname: String = "Lastname"
  ): RegisterAccountRequest =
    RegisterAccountRequest(username, password, email, firstname, lastname)
}
