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
        addedProject <- projectRepository.addProject(getMockProject(ownerId = addedAccount.id))
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
  }

  "getProjectById" should {
    "return the project by id" in {
      val projectRepository = new ProjectRepository(Some(getPGDatabase))
      val accountRepository = new AccountRepository(Some(getPGDatabase))
      val mockAccount = getMockAccount()

      val futureProject = for {
        addedAccount <- accountRepository.addAccount(mockAccount)
        addedProject <- projectRepository.addProject(getMockProject(ownerId = addedAccount.id))
        returnedProject <- projectRepository.getProjectById(addedProject.projectId)
      } yield returnedProject

      futureProject.map { actual =>
        actual.isDefined shouldBe true
        actual.value.name shouldEqual "my-project-1"
      }
    }
  }
}
