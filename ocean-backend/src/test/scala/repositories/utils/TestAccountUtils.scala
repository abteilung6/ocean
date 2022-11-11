package org.abteilung6.ocean
package repositories.utils

import repositories.dto.Account

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
    lastLoginAt: Option[Instant] = Some(Instant.now()),
    expiresAt: Option[Instant] = Some(Instant.now())
  ): Account =
    Account(id, username, email, firstname, lastname, employeeType, createdAt, lastLoginAt, expiresAt)
}
