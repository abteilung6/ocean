package org.abteilung6.ocean
package repositories

import repositories.dto.{ Account, AuthenticatorType }
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape
import java.sql.Timestamp
import java.time.Instant
import scala.concurrent.Future

class AccountRepository(patchDatabase: Option[Database] = None) {

  private val db = patchDatabase match {
    case Some(value) => value
    case None        => Database.forConfig("postgres-internal")
  }

  // Since we store an Instant as a Timestamp in postgres,
  // we need to implicitly declare this conversion.
  implicit val instantColumnType: BaseColumnType[Instant] =
    MappedColumnType.base[Instant, Timestamp](
      instant => Timestamp.from(instant),
      ts => ts.toInstant
    )

  implicit val authenticatorTypeColumnType: BaseColumnType[AuthenticatorType] =
    MappedColumnType.base[AuthenticatorType, String](
      e => e.entryName,
      s => AuthenticatorType.withName(s)
    )

  class AccountTable(tag: Tag) extends Table[Account](tag, "accounts") {
    def accountId: Rep[Long] = column[Long]("account_id", O.PrimaryKey, O.AutoInc)

    def username: Rep[String] = column[String]("username")

    def email: Rep[String] = column[String]("email", O.Unique)

    def firstname: Rep[String] = column[String]("firstname")

    def lastname: Rep[String] = column[String]("lastname")

    def employeeType: Rep[String] = column[String]("employee_type")

    def createdAt: Rep[Instant] = column[Instant]("created_at")

    def authenticatorType: Rep[AuthenticatorType] = column[AuthenticatorType]("authenticator_type")

    def verified: Rep[Boolean] = column[Boolean]("verified")

    def passwordHash: Rep[Option[String]] = column[Option[String]]("password_hash")

    def * : ProvenShape[Account] =
      (
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
      ) <> ((Account.apply _).tupled, Account.unapply)

    def indexUsernameAuthenticatorType =
      index("idx_username_authenticator_type", (username, authenticatorType), unique = true)
  }

  val accounts = TableQuery[AccountTable]

  def getAccountById(accountId: Long): Future[Option[Account]] = db.run(
    accounts.filter(_.accountId === accountId).result.headOption
  )

  def getAccountByUsername(username: String, authenticatorType: AuthenticatorType): Future[Option[Account]] = db.run(
    accounts
      .filter(account => account.username === username && account.authenticatorType === authenticatorType)
      .result
      .headOption
  )

  def getAccountByEmail(email: String): Future[Option[Account]] = db.run(
    accounts.filter(_.email === email).result.headOption
  )

  def addAccount(account: Account): Future[Account] = db.run(
    accounts.returning(accounts) += account
  )

  def verifyAccountById(accountId: Long): Future[Int] = db.run(
    accounts.filter(_.accountId === accountId).map(_.verified).update(true)
  )
}
