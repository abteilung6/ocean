package org.abteilung6.ocean
package repositories.utils

import repositories.Account
import java.sql.Timestamp
import java.time.LocalDateTime

object TestAccountUtils {

  def getDummyAccount(
    id: Long = 0L,
    username: String = "username1",
    email: String = "username1@localhost",
    firstname: String = "firstname1",
    lastname: String = "lastname2",
    employeeType: String = "student",
    createdAt: Timestamp = Timestamp.valueOf(LocalDateTime.now()),
    lastLoginAt: Option[Timestamp] = Some(Timestamp.valueOf(LocalDateTime.now())),
    expiresAt: Option[Timestamp] = Some(Timestamp.valueOf(LocalDateTime.now()))
  ): Account =
    Account(id, username, email, firstname, lastname, employeeType, createdAt, lastLoginAt, expiresAt)
}
