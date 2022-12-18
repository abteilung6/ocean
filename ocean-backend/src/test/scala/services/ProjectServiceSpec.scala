package org.abteilung6.ocean
package services

import repositories.utils.TestMockUtils.{ getMockAccount, getMockMemberResponse, getMockProject }
import repositories.{ MemberRepository, ProjectRepository }
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.{ ExecutionContext, ExecutionContextExecutor, Future }

class ProjectServiceSpec extends AsyncWordSpecLike with Matchers with MockitoSugar with BeforeAndAfterEach {
  override implicit val executionContext: ExecutionContextExecutor = ExecutionContext.global

  var defaultMemberRepository: MemberRepository = mock[MemberRepository]
  var defaultProjectRepository: ProjectRepository = mock[ProjectRepository]

  override def beforeEach(): Unit = {
    defaultMemberRepository = mock[MemberRepository]
    defaultProjectRepository = mock[ProjectRepository]
  }

  private def createProjectService(
    memberRepository: MemberRepository = defaultMemberRepository,
    projectRepository: ProjectRepository = defaultProjectRepository
  ): ProjectService =
    new ProjectService(projectRepository, memberRepository)

  "getProjects" should {
    "return projects" in {
      val project = getMockProject()
      val account = getMockAccount()
      val projectService = createProjectService()

      when(defaultProjectRepository.getProjectsByAccountId(ArgumentMatchers.eq(account.accountId)))
        .thenReturn(Future(Seq(project)))

      projectService.getProjects(account.accountId).map { projects =>
        projects shouldBe Seq(project)
      }
    }
  }

  "getProject" should {
    "get project for account" in {
      val mockProject = getMockProject()
      val account = getMockAccount()
      val projectService = createProjectService()

      when(defaultProjectRepository.getProjectById(ArgumentMatchers.eq(mockProject.projectId)))
        .thenReturn(Future(Some(mockProject)))
      when(defaultMemberRepository.getMembersByProjectId(ArgumentMatchers.eq(mockProject.projectId)))
        .thenReturn(
          Future(Seq(getMockMemberResponse(projectId = mockProject.projectId, accountId = account.accountId)))
        )

      projectService.getProject(account.accountId, mockProject.projectId).map { project =>
        project shouldBe mockProject
      }
    }
  }
}
