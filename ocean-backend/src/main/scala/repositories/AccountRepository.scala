package org.abteilung6.ocean
package repositories

import repositories.dto.Account
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

  class AccountTable(tag: Tag) extends Table[Account](tag, "accounts") {
    def accountId: Rep[Long] = column[Long]("account_id", O.PrimaryKey, O.AutoInc)

    def username: Rep[String] = column[String]("username", O.Unique)

    def email: Rep[String] = column[String]("email", O.Unique)

    def firstname: Rep[String] = column[String]("firstname")

    def lastname: Rep[String] = column[String]("lastname")

    def employeeType: Rep[String] = column[String]("employee_type")

    def createdAt: Rep[Instant] = column[Instant]("created_at")

    def lastLoginAt: Rep[Option[Instant]] = column[Option[Instant]]("last_login_at")

    def expiresAt: Rep[Option[Instant]] = column[Option[Instant]]("expires_at")

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

  def getAccountByUsername(username: String): Future[Option[Account]] = db.run(
    accounts.filter(_.username === username).result.headOption
  )

  def getAccountById(accountId: Long): Future[Option[Account]] = db.run(
    accounts.filter(_.accountId === accountId).result.headOption
  )

  def addAccount(account: Account): Future[Account] = db.run(
    accounts.returning(accounts) += account
  )
}
