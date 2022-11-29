package org.abteilung6.ocean
package repositories

import org.abteilung6.ocean.repositories.dto.AuthenticatorType
import utils.TestDatabase
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike
import slick.jdbc.JdbcBackend.Database

class AccountRepositorySpec extends TestDatabase with AsyncWordSpecLike with Matchers with BeforeAndAfterEach {

  import utils.TestMockUtils._

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
      val dummyAccount = getMockAccount(id = 1)
      val database = getDatabase()
      val accountRepository = new AccountRepository(Some(database))
      val futureAccount = accountRepository.addAccount(dummyAccount)

      futureAccount.map { actual =>
        actual shouldEqual dummyAccount
      }
    }
  }

  "getAccountByUsername" should {
    "return an account by username and authenticator type" in {
      val database = getDatabase()
      val accountRepository = new AccountRepository(Some(database))
      val dummyAccount = getMockAccount(id = 1, authenticatorType = AuthenticatorType.Directory)

      val result = for {
        _ <- accountRepository.addAccount(dummyAccount)
        byUsername <- accountRepository.getAccountByUsername(dummyAccount.username, AuthenticatorType.Directory)
      } yield byUsername

      result.map { optUser =>
        optUser shouldEqual Some(dummyAccount)
      }
    }
  }

  "verifyAccountById" should {
    "verifies an account" in {
      val dummyAccount = getMockAccount(id = 1, verified = false)
      val database = getDatabase()
      val accountRepository = new AccountRepository(Some(database))

      val result = for {
        addedAccount <- accountRepository.addAccount(dummyAccount)
        updatedRows <- accountRepository.verifyAccountById(addedAccount.id)
        updatedAccount <- accountRepository.getAccountById(addedAccount.id)
      } yield (updatedRows, updatedAccount)

      result.map { tuple =>
        tuple._1 shouldEqual 1
        tuple._2.get.verified shouldBe true
      }
    }
  }
}
