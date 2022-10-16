package org.abteilung6.ocean
package repositories

import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape

import java.sql.Timestamp
import scala.concurrent.Future

case class Account(
  id: Long,
  username: String,
  email: String,
  firstname: String,
  lastname: String,
  employeeType: String,
  createdAt: Timestamp,
  lastLoginAt: Option[Timestamp],
  expiresAt: Option[Timestamp]
)

class AccountRepository(patchDatabase: Option[Database] = None) {

  private val db = patchDatabase match {
    case Some(value) => value
    case None        => Database.forConfig("postgres-internal")
  }

  class AccountTable(tag: Tag) extends Table[Account](tag, "accounts") {
    def accountId: Rep[Long] = column[Long]("account_id", O.PrimaryKey, O.AutoInc)

    def username: Rep[String] = column[String]("username", O.Unique)

    def email: Rep[String] = column[String]("email", O.Unique)

    def firstname: Rep[String] = column[String]("firstname")

    def lastname: Rep[String] = column[String]("lastname")

    def employeeType: Rep[String] = column[String]("employee_type")

    def createdAt: Rep[Timestamp] = column[Timestamp]("created_at")

    def lastLoginAt: Rep[Option[Timestamp]] = column[Option[Timestamp]]("last_login_at")

    def expiresAt: Rep[Option[Timestamp]] = column[Option[Timestamp]]("expires_at")

    def * : ProvenShape[Account] =
      (
        accountId,
        username,
        email,
        firstname,
        lastname,
        employeeType,
        createdAt,
        lastLoginAt,
        expiresAt
      ) <> ((Account.apply _).tupled, Account.unapply)
  }

  val accounts = TableQuery[AccountTable]

  def getUserByUsername(username: String): Future[Option[Account]] = db.run(
    accounts.filter(_.username === username).result.headOption
  )

  def addAccount(account: Account): Future[Account] = db.run(
    accounts.returning(accounts) += account
  )
}
