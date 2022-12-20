package org.abteilung6.ocean
package repositories

import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException
import utils.TestDatabase
import org.scalatest.{ BeforeAndAfterEach, OptionValues }
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike

class ProjectRepositorySpec
    extends TestDatabase("projects")
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
    "return the project by projectId" in {
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

  "getProjectsByAccountId" should {
    "return projects only related to the account" in {
      val projectRepository = new ProjectRepository(Some(getPGDatabase))
      val accountRepository = new AccountRepository(Some(getPGDatabase))
      val memberRepository = new MemberRepository((Some(getPGDatabase)))
      val myMockAccount = getMockAccount(username = "username1", email = "email1")
      val anotherMockAccount = getMockAccount(username = "username2", email = "email2")

      val futureProjects = for {
        myAccount <- accountRepository.addAccount(myMockAccount)
        anotherAccount <- accountRepository.addAccount(anotherMockAccount)
        myProject <- projectRepository.addProject(getMockProject(name = "project1", ownerId = myAccount.accountId))
        anotherProject <- projectRepository.addProject(
          getMockProject(name = "project2", ownerId = anotherAccount.accountId)
        )
        _ <- memberRepository.addMember(
          getMockMember(projectId = myProject.projectId, accountId = myAccount.accountId)
        )
        _ <- memberRepository.addMember(
          getMockMember(projectId = anotherProject.projectId, accountId = anotherAccount.accountId)
        )
        myProjects <- projectRepository.getProjectsByAccountId(myAccount.accountId)
      } yield myProjects

      futureProjects.map { projects =>
        projects.length shouldBe 1
        projects.head.name shouldBe "project1"
      }
    }
  }
}
