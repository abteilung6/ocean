package org.abteilung6.ocean
package services

import repositories.utils.TestMockUtils.{ getMockAccount, getMockMemberResponse, getMockProject }
import repositories.{ MemberRepository, ProjectRepository }
import repositories.dto.Account
import repositories.dto.project.{ MemberState, Project, RoleType }
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.{ ExecutionContext, ExecutionContextExecutor, Future }

class ProjectServiceSpec extends AsyncWordSpecLike with Matchers with MockitoSugar with BeforeAndAfterEach {
  override implicit val executionContext: ExecutionContextExecutor = ExecutionContext.global

  var defaultAccount: Account = getMockAccount(accountId = 100)
  var defaultProject: Project = getMockProject(projectId = 200)
  var defaultMemberRepository: MemberRepository = mock[MemberRepository]
  var defaultProjectRepository: ProjectRepository = mock[ProjectRepository]

  override def beforeEach(): Unit = {
    defaultAccount = getMockAccount(accountId = 99)
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
      val projectService = createProjectService()

      when(defaultProjectRepository.getProjectById(ArgumentMatchers.eq(defaultProject.projectId)))
        .thenReturn(Future(Some(defaultProject)))
      when(defaultMemberRepository.getMembersByProjectId(ArgumentMatchers.eq(defaultProject.projectId)))
        .thenReturn(
          Future(Seq(getMockMemberResponse(projectId = defaultProject.projectId, accountId = defaultAccount.accountId)))
        )

      projectService.getProject(defaultAccount.accountId, defaultProject.projectId).map { project =>
        project shouldBe defaultProject
      }
    }
  }

  "deleteProject" should {
    "delete project" in {
      val project = getMockProject(projectId = 10)
      val account = getMockAccount(accountId = 20)
      val memberResponse = getMockMemberResponse(
        memberId = 30,
        accountId = account.accountId,
        projectId = project.projectId,
        state = MemberState.Active,
        roleType = RoleType.Admin
      )
      val projectService = createProjectService()

      when(defaultProjectRepository.getProjectById(ArgumentMatchers.eq(project.projectId)))
        .thenReturn(Future(Some(project)))
      when(
        defaultMemberRepository.getMemberByAccountId(
          ArgumentMatchers.eq(account.accountId),
          ArgumentMatchers.eq(project.projectId)
        )
      )
        .thenReturn(Future(Some(memberResponse)))
      when(defaultMemberRepository.deleteMembersByProjectId(ArgumentMatchers.eq(project.projectId)))
        .thenReturn(Future(1))
      when(defaultProjectRepository.deleteProject(ArgumentMatchers.eq(project.projectId)))
        .thenReturn(Future(1))

      projectService.deleteProject(project.projectId, account).map { result =>
        result shouldBe 1
      }
    }

    List(
      getMockMemberResponse(
        memberId = 30,
        accountId = defaultAccount.accountId,
        projectId = defaultProject.projectId,
        state = MemberState.Pending,
        roleType = RoleType.Admin
      ),
      getMockMemberResponse(
        memberId = 30,
        accountId = defaultAccount.accountId,
        projectId = defaultProject.projectId,
        state = MemberState.Active,
        roleType = RoleType.Developer
      ),
      getMockMemberResponse(
        memberId = 30,
        accountId = defaultAccount.accountId,
        projectId = defaultProject.projectId,
        state = MemberState.Active,
        roleType = RoleType.Viewer
      )
    ).foreach { member =>
      s"return ${ProjectService.Exceptions.InsufficientPermissionException.toString} with state ${member.state} and roleType ${member.roleType}" in {
        val projectService = createProjectService()

        when(defaultProjectRepository.getProjectById(ArgumentMatchers.eq(defaultProject.projectId)))
          .thenReturn(Future(Some(defaultProject)))
        when(
          defaultMemberRepository.getMemberByAccountId(
            ArgumentMatchers.eq(defaultAccount.accountId),
            ArgumentMatchers.eq(defaultProject.projectId)
          )
        )
          .thenReturn(Future(Some(member)))
        when(defaultMemberRepository.deleteMembersByProjectId(ArgumentMatchers.eq(defaultProject.projectId)))
          .thenReturn(Future(1))
        when(defaultProjectRepository.deleteProject(ArgumentMatchers.eq(defaultProject.projectId)))
          .thenReturn(Future(1))

        projectService.deleteProject(defaultProject.projectId, defaultAccount).failed.map { result =>
          result.isInstanceOf[ProjectService.Exceptions.InsufficientPermissionException] shouldBe true
        }
      }
    }
  }
}
