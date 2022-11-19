package org.abteilung6.ocean
package repositories.utils

import repositories.dto.{ Account, AuthenticatorType }

import java.time.Instant

object TestAccountUtils {

  def getDummyAccount(
    id: Long = 0L,
    username: String = "username1",
    email: String = "username1@localhost",
    firstname: String = "firstname1",
    lastname: String = "lastname2",
    employeeType: String = "student",
    createdAt: Instant = Instant.now(),
    authenticatorType: AuthenticatorType = AuthenticatorType.Directory
  ): Account =
    Account(
      id,
      username,
      email,
      firstname,
      lastname,
      employeeType,
      createdAt,
      authenticatorType
    )
}
