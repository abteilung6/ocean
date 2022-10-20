package org.abteilung6.ocean
package repositories

import utils.TestDatabase
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike
import slick.jdbc.JdbcBackend.Database

class AccountRepositorySpec extends TestDatabase with AsyncWordSpecLike with Matchers with BeforeAndAfterEach {

  import utils.TestAccountUtils._

  override def beforeEach(): Unit = {
    super.beforeEach()
    this.deploy()
  }

  override def afterEach(): Unit = {
    super.afterEach()
    this.teardown()
  }

  private def getDatabase() =
    Database.forURL(connectionString, driver = "org.postgresql.ds.PGSimpleDataSource")

  "addAccount" should {
    "creates an account" in {
      val dummyAccount = getDummyAccount(id = 1)
      val database = getDatabase()
      val accountRepository = new AccountRepository(Some(database))
      val futureAccount = accountRepository.addAccount(dummyAccount)

      futureAccount.map { actual =>
        actual shouldEqual dummyAccount
      }
    }
  }

  "getAccountByUsername" should {
    "return an account by username" in {
      val database = getDatabase()
      val accountRepository = new AccountRepository(Some(database))
      val dummyAccount = getDummyAccount(id = 1)

      val result = for {
        _ <- accountRepository.addAccount(dummyAccount)
        byUsername <- accountRepository.getAccountByUsername(dummyAccount.username)
      } yield byUsername

      result.map { optUser =>
        optUser shouldEqual Some(dummyAccount)
      }
    }
  }
}
