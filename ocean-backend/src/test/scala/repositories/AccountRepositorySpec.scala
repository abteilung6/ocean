package org.abteilung6.ocean
package repositories

import repositories.dto.AuthenticatorType
import utils.TestDatabase
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike

class AccountRepositorySpec
    extends TestDatabase("accounts")
    with AsyncWordSpecLike
    with Matchers
    with BeforeAndAfterEach {

  import utils.TestMockUtils._

  override def beforeEach(): Unit = {
    super.beforeEach()
    this.deploy()
  }

  override def afterEach(): Unit = {
    super.afterEach()
    this.teardown()
  }

  "addAccount" should {
    "creates an account" in {
      val dummyAccount = getMockAccount(accountId = 1)
      val accountRepository = new AccountRepository(Some(getPGDatabase))
      val futureAccount = accountRepository.addAccount(dummyAccount)

      futureAccount.map { addedAccount =>
        addedAccount.username shouldBe dummyAccount.username
      }
    }
  }

  "getAccountByUsername" should {
    "return an account by username and authenticator type" in {
      val accountRepository = new AccountRepository(Some(getPGDatabase))
      val dummyAccount = getMockAccount(accountId = 1)

      val result = for {
        _ <- accountRepository.addAccount(dummyAccount)
        byUsername <- accountRepository.getAccountByUsername(dummyAccount.username, AuthenticatorType.Credentials)
      } yield byUsername

      result.map { addedAccount =>
        addedAccount.isDefined shouldBe true
      }
    }
  }

  "verifyAccountById" should {
    "verifies an account" in {
      val dummyAccount = getMockAccount(accountId = 1, verified = false)
      val accountRepository = new AccountRepository(Some(getPGDatabase))

      val result = for {
        addedAccount <- accountRepository.addAccount(dummyAccount)
        updatedRows <- accountRepository.verifyAccountById(addedAccount.accountId)
        updatedAccount <- accountRepository.getAccountById(addedAccount.accountId)
      } yield (updatedRows, updatedAccount)

      result.map { tuple =>
        tuple._1 shouldEqual 1
        tuple._2.get.verified shouldBe true
      }
    }
  }
}
