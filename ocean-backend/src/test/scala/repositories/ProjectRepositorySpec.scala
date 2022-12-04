package org.abteilung6.ocean
package repositories

import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException
import utils.TestDatabase
import org.scalatest.{ BeforeAndAfterEach, OptionValues }
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike

class ProjectRepositorySpec
    extends TestDatabase
    with AsyncWordSpecLike
    with Matchers
    with BeforeAndAfterEach
    with OptionValues {

  import utils.TestMockUtils._

  override def beforeEach(): Unit = {
    super.beforeEach()
    this.deploy()
  }

  override def afterEach(): Unit = {
    super.afterEach()
    this.teardown()
  }

  "addProject" should {
    "create a project with owner" in {
      val projectRepository = new ProjectRepository(Some(getPGDatabase))
      val accountRepository = new AccountRepository(Some(getPGDatabase))
      val mockAccount = getMockAccount()

      val futureProject = for {
        addedAccount <- accountRepository.addAccount(mockAccount)
        addedProject <- projectRepository.addProject(getMockProject(ownerId = addedAccount.accountId))
      } yield addedProject

      futureProject.map { actual =>
        actual.name shouldEqual "my-project-1"
      }
    }

    "check referential integrity constraint violation for owner" in {
      val projectRepository = new ProjectRepository(Some(getPGDatabase))
      val mockProject = getMockProject(ownerId = 42)

      recoverToSucceededIf[JdbcSQLIntegrityConstraintViolationException] {
        projectRepository.addProject(mockProject)
      }
    }

    "check unique constraint for name" in {
      val projectRepository = new ProjectRepository(Some(getPGDatabase))
      val accountRepository = new AccountRepository(Some(getPGDatabase))
      val mockAccount = getMockAccount()

      recoverToSucceededIf[JdbcSQLIntegrityConstraintViolationException] {
        val futureProject = for {
          addedAccount <- accountRepository.addAccount(mockAccount)
          addedProject <- projectRepository.addProject(getMockProject(name = "p1", ownerId = addedAccount.accountId))
          duplicatedProject <- projectRepository.addProject(
            getMockProject(name = "p1", ownerId = addedAccount.accountId)
          )
        } yield (addedProject, duplicatedProject)
        futureProject
      }
    }
  }

  "getProjectById" should {
    "return the project by id" in {
      val projectRepository = new ProjectRepository(Some(getPGDatabase))
      val accountRepository = new AccountRepository(Some(getPGDatabase))
      val mockAccount = getMockAccount()

      val futureProject = for {
        addedAccount <- accountRepository.addAccount(mockAccount)
        addedProject <- projectRepository.addProject(getMockProject(ownerId = addedAccount.accountId))
        returnedProject <- projectRepository.getProjectById(addedProject.projectId)
      } yield returnedProject

      futureProject.map { actual =>
        actual.isDefined shouldBe true
        actual.value.name shouldEqual "my-project-1"
      }
    }
  }
}
