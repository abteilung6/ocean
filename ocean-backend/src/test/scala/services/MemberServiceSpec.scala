package org.abteilung6.ocean
package services

import repositories.dto.project.{ CreateMemberRequest, Member, MemberState, MemberVerificationTokenContent, RoleType }
import repositories.utils.TestMockUtils.{ getMockAccount, getMockMemberResponse, getMockProject }
import repositories.{ MemberRepository, ProjectRepository }
import services.MemberService.Exceptions.{
  InsufficientPermissionException,
  MemberExistsException,
  ProjectDoesNotExistException
}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.{ any, anyLong }
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import scala.concurrent.{ ExecutionContext, ExecutionContextExecutor, Future }

class MemberServiceSpec extends AsyncWordSpecLike with Matchers with MockitoSugar with BeforeAndAfterEach {
  override implicit val executionContext: ExecutionContextExecutor = ExecutionContext.global

  var defaultMemberRepository: MemberRepository = mock[MemberRepository]
  var defaultProjectRepository: ProjectRepository = mock[ProjectRepository]

  override def beforeEach(): Unit = {
    defaultMemberRepository = mock[MemberRepository]
    defaultProjectRepository = mock[ProjectRepository]
  }

  private def createMemberService(
    memberRepository: MemberRepository = defaultMemberRepository,
    projectRepository: ProjectRepository = defaultProjectRepository
  ): MemberService =
    new MemberService(memberRepository, projectRepository)

  "createMember" should {

    "initially create a member as owner" in {
      val invitorAccount = getMockAccount(accountId = 10)
      val mockProject = getMockProject(projectId = 20, ownerId = invitorAccount.accountId)
      val mockMemberResponse = getMockMemberResponse(
        memberId = 30L,
        roleType = RoleType.Admin,
        state = MemberState.Active,
        accountId = invitorAccount.accountId,
        projectId = mockProject.projectId
      )
      val memberService = createMemberService()
      val createMemberRequest = CreateMemberRequest(mockProject.projectId, invitorAccount.accountId, RoleType.Developer)

      when(defaultProjectRepository.getProjectById(ArgumentMatchers.eq(mockProject.projectId)))
        .thenReturn(Future(Some(mockProject)))
      when(defaultMemberRepository.getMemberByAccountId(ArgumentMatchers.eq(invitorAccount.accountId), anyLong()))
        .thenReturn(Future(None))
      when(defaultMemberRepository.addMember(any[Member]()))
        .thenReturn(Future(mockMemberResponse))

      memberService.createMember(createMemberRequest, invitorAccount, forOwner = true).map { memberResponse =>
        memberResponse.accountId shouldBe invitorAccount.accountId
      }
    }

    "return ProjectDoesNotExistException if project does not exist" in {
      val invitorAccount = getMockAccount()
      val mockMemberResponse = getMockMemberResponse()
      val memberService = createMemberService()
      val createMemberRequest =
        CreateMemberRequest(0L, mockMemberResponse.accountId, RoleType.Developer)

      when(defaultProjectRepository.getProjectById(ArgumentMatchers.eq(0L)))
        .thenReturn(Future(None))
      when(defaultMemberRepository.getMemberByAccountId(anyLong(), anyLong()))
        .thenReturn(Future(Some(mockMemberResponse)))

      memberService.createMember(createMemberRequest, invitorAccount).failed.map { exception =>
        exception.isInstanceOf[ProjectDoesNotExistException] shouldBe true
      }
    }

    "return InsufficientPermissionException if account is not owner of the project" in {
      val invitorAccount = getMockAccount(accountId = 10)
      val mockProject = getMockProject(projectId = 20, ownerId = invitorAccount.accountId + 1)
      val createMemberRequest =
        CreateMemberRequest(mockProject.projectId, invitorAccount.accountId, RoleType.Developer)
      val memberService = createMemberService()

      when(defaultProjectRepository.getProjectById(ArgumentMatchers.eq(mockProject.projectId)))
        .thenReturn(Future(Some(mockProject)))
      when(defaultMemberRepository.getMemberByAccountId(ArgumentMatchers.eq(invitorAccount.accountId), anyLong()))
        .thenReturn(Future(None))

      memberService.createMember(createMemberRequest, invitorAccount, forOwner = true).failed.map { exception =>
        exception.isInstanceOf[InsufficientPermissionException] shouldBe true
        exception.getMessage shouldBe "You are not owner of this project."
      }
    }

    "return MemberExistsException if owner is already a member of the project" in {
      val invitorAccount = getMockAccount(accountId = 10)
      val invitedAccount = getMockAccount(accountId = 20)
      val mockProject = getMockProject(projectId = 30, ownerId = invitorAccount.accountId)
      val createMemberRequest =
        CreateMemberRequest(mockProject.projectId, invitedAccount.accountId, RoleType.Developer)
      val invitorMemberResponse =
        getMockMemberResponse(
          accountId = invitedAccount.accountId,
          projectId = mockProject.projectId,
          roleType = RoleType.Admin
        )
      val invitedMemberResponse =
        getMockMemberResponse(accountId = invitedAccount.accountId, projectId = mockProject.projectId)
      val memberService = createMemberService()

      when(defaultProjectRepository.getProjectById(ArgumentMatchers.eq(mockProject.projectId)))
        .thenReturn(Future(Some(mockProject)))
      when(defaultMemberRepository.getMemberByAccountId(ArgumentMatchers.eq(invitorAccount.accountId), anyLong()))
        .thenReturn(Future(Some(invitorMemberResponse)))
      when(defaultMemberRepository.getMemberByAccountId(ArgumentMatchers.eq(invitedAccount.accountId), anyLong()))
        .thenReturn(Future(Some(invitedMemberResponse)))

      memberService.createMember(createMemberRequest, invitorAccount).failed.map { exception =>
        exception.isInstanceOf[MemberExistsException] shouldBe true
        exception.getMessage shouldBe "Account is already member of this project."
      }
    }

    "create a new member as a member with admin privilege" in {
      val mockAccount = getMockAccount(accountId = 10)
      val invitedAccount = getMockAccount(accountId = 20)
      val mockProject = getMockProject(projectId = 30, ownerId = 0L)
      val invitorMemberResponse = getMockMemberResponse(
        accountId = mockAccount.accountId,
        projectId = mockProject.projectId,
        roleType = RoleType.Admin
      )
      val invitedMemberResponse = getMockMemberResponse(
        accountId = invitedAccount.accountId,
        projectId = mockProject.projectId,
        roleType = RoleType.Admin
      )
      val createMemberRequest =
        CreateMemberRequest(mockProject.projectId, invitedAccount.accountId, RoleType.Admin)
      val memberService = createMemberService()

      when(defaultProjectRepository.getProjectById(ArgumentMatchers.eq(mockProject.projectId)))
        .thenReturn(Future(Some(mockProject)))
      when(defaultMemberRepository.getMemberByAccountId(ArgumentMatchers.eq(mockAccount.accountId), anyLong()))
        .thenReturn(Future(Some(invitorMemberResponse)))
      when(defaultMemberRepository.getMemberByAccountId(ArgumentMatchers.eq(invitedAccount.accountId), anyLong()))
        .thenReturn(Future(None))
      when(defaultMemberRepository.addMember(any[Member]()))
        .thenReturn(Future(invitedMemberResponse))

      memberService.createMember(createMemberRequest, mockAccount).map { memberResponse =>
        memberResponse.accountId shouldBe invitedAccount.accountId
        memberResponse.projectId shouldBe mockProject.projectId
      }
    }

    "return InsufficientPermissionException if invitor is not a member of this project" in {
      val invitorAccount = getMockAccount(accountId = 10)
      val invitedAccount = getMockAccount(accountId = 20)
      val mockProject = getMockProject(projectId = 30, ownerId = 0L)
      val invitedMemberResponse = getMockMemberResponse(
        accountId = invitedAccount.accountId,
        projectId = mockProject.projectId,
        roleType = RoleType.Admin
      )
      val createMemberRequest =
        CreateMemberRequest(mockProject.projectId, invitedAccount.accountId, RoleType.Admin)
      val memberService = createMemberService()

      when(defaultProjectRepository.getProjectById(ArgumentMatchers.eq(mockProject.projectId)))
        .thenReturn(Future(Some(mockProject)))
      when(defaultMemberRepository.getMemberByAccountId(ArgumentMatchers.eq(invitorAccount.accountId), anyLong()))
        .thenReturn(Future(None))
      when(defaultMemberRepository.getMemberByAccountId(ArgumentMatchers.eq(invitedAccount.accountId), anyLong()))
        .thenReturn(Future(Some(invitedMemberResponse)))

      memberService.createMember(createMemberRequest, invitorAccount).failed.map { exception =>
        exception.isInstanceOf[InsufficientPermissionException] shouldBe true
        exception.getMessage shouldBe "You are not a member of this project."
      }
    }

    "return InsufficientPermissionException if invitor has not admin role" in {
      val invitorAccount = getMockAccount(accountId = 10)
      val invitedAccount = getMockAccount(accountId = 20)
      val mockProject = getMockProject(projectId = 30, ownerId = 0L)
      val invitorMemberResponse = getMockMemberResponse(
        accountId = invitedAccount.accountId,
        projectId = mockProject.projectId,
        roleType = RoleType.Developer
      )
      val createMemberRequest =
        CreateMemberRequest(mockProject.projectId, invitedAccount.accountId, RoleType.Admin)
      val memberService = createMemberService()

      when(defaultProjectRepository.getProjectById(ArgumentMatchers.eq(mockProject.projectId)))
        .thenReturn(Future(Some(mockProject)))
      when(defaultMemberRepository.getMemberByAccountId(ArgumentMatchers.eq(invitorAccount.accountId), anyLong()))
        .thenReturn(Future(Some(invitorMemberResponse)))
      when(defaultMemberRepository.getMemberByAccountId(ArgumentMatchers.eq(invitedAccount.accountId), anyLong()))
        .thenReturn(Future(None))

      memberService.createMember(createMemberRequest, invitorAccount).failed.map { exception =>
        exception.isInstanceOf[InsufficientPermissionException] shouldBe true
        exception.getMessage shouldBe s"Only members with role ${RoleType.Admin.entryName} can invite new members to project."
      }
    }

    "return MemberExistsException if invited account is already member of this project" in {
      val invitorAccount = getMockAccount(accountId = 10)
      val invitedAccount = getMockAccount(accountId = 20)
      val mockProject = getMockProject(projectId = 30, ownerId = 0L)
      val invitorMemberResponse = getMockMemberResponse(
        accountId = invitedAccount.accountId,
        projectId = mockProject.projectId,
        roleType = RoleType.Admin
      )
      val invitedMemberResponse = getMockMemberResponse(
        accountId = invitedAccount.accountId,
        projectId = mockProject.projectId
      )
      val createMemberRequest =
        CreateMemberRequest(mockProject.projectId, invitedAccount.accountId, RoleType.Admin)
      val memberService = createMemberService()

      when(defaultProjectRepository.getProjectById(ArgumentMatchers.eq(mockProject.projectId)))
        .thenReturn(Future(Some(mockProject)))
      when(defaultMemberRepository.getMemberByAccountId(ArgumentMatchers.eq(invitorAccount.accountId), anyLong()))
        .thenReturn(Future(Some(invitorMemberResponse)))
      when(defaultMemberRepository.getMemberByAccountId(ArgumentMatchers.eq(invitedAccount.accountId), anyLong()))
        .thenReturn(Future(Some(invitedMemberResponse)))

      memberService.createMember(createMemberRequest, invitorAccount).failed.map { exception =>
        exception.isInstanceOf[MemberExistsException] shouldBe true
        exception.getMessage shouldBe s"Account is already member of this project."
      }
    }
  }

  "acceptMember" should {
    "return accepted member" in {
      val invitedMember = getMockMemberResponse(state = MemberState.Pending)
      val acceptedMember = getMockMemberResponse(state = MemberState.Active)
      val project = getMockProject()
      val memberVerificationTokenContent = MemberVerificationTokenContent("", invitedMember.memberId)
      val memberService = createMemberService()

      when(defaultMemberRepository.getMemberById(ArgumentMatchers.eq(invitedMember.memberId)))
        .thenReturn(Future(Some(invitedMember)))
      when(defaultMemberRepository.acceptMemberById(ArgumentMatchers.eq(invitedMember.memberId)))
        .thenReturn(Future(Some(acceptedMember)))
      when(defaultProjectRepository.getProjectById(ArgumentMatchers.eq(project.projectId)))
        .thenReturn(Future(Some(project)))

      memberService.acceptMember(memberVerificationTokenContent).map { memberResponse =>
        memberResponse shouldBe acceptedMember
      }
    }
  }

  "deleteMember" should {
    "delete the member" in {
      val project = getMockProject()
      val account = getMockAccount(accountId = 10L)
      val accountToDelete = getMockAccount(accountId = 20L)
      val memberResponse =
        getMockMemberResponse(accountId = account.accountId, memberId = 30L, roleType = RoleType.Admin)
      val memberToDeleteResponse =
        getMockMemberResponse(accountId = accountToDelete.accountId, memberId = 40L, roleType = RoleType.Admin)
      val memberService = createMemberService()

      when(defaultMemberRepository.getMemberById(ArgumentMatchers.eq(memberToDeleteResponse.memberId)))
        .thenReturn(Future(Some(memberToDeleteResponse)))
      when(
        defaultMemberRepository.getMemberByAccountId(
          ArgumentMatchers.eq(account.accountId),
          ArgumentMatchers.eq(project.projectId)
        )
      )
        .thenReturn(Future(Some(memberResponse)))
      when(defaultProjectRepository.getProjectById(ArgumentMatchers.eq(project.projectId)))
        .thenReturn(Future(Some(project)))
      when(defaultMemberRepository.getMembersByProjectId(ArgumentMatchers.eq(project.projectId)))
        .thenReturn(Future(Seq(memberResponse, memberToDeleteResponse)))
      when(defaultMemberRepository.deleteMemberById(ArgumentMatchers.eq(memberToDeleteResponse.memberId)))
        .thenReturn(Future(1))

      memberService.deleteMember(memberToDeleteResponse.memberId, account).map { rows =>
        rows shouldBe 1
      }
    }

    List(
      Tuple2(MemberState.Pending, RoleType.Admin),
      Tuple2(MemberState.Pending, RoleType.Developer),
      Tuple2(MemberState.Pending, RoleType.Viewer),
      Tuple2(MemberState.Active, RoleType.Developer),
      Tuple2(MemberState.Active, RoleType.Viewer)
    ).foreach { case (state, roleType) =>
      s"return ${InsufficientPermissionException.toString} with state ${state} and roleType $roleType}" in {
        val project = getMockProject()
        val account = getMockAccount(accountId = 10L)
        val accountToDelete = getMockAccount(accountId = 20L)
        val memberResponse =
          getMockMemberResponse(accountId = account.accountId, memberId = 30L, state = state, roleType = roleType)
        val memberToDeleteResponse =
          getMockMemberResponse(accountId = accountToDelete.accountId, memberId = 40L, roleType = RoleType.Admin)
        val memberService = createMemberService()

        when(defaultMemberRepository.getMemberById(ArgumentMatchers.eq(memberToDeleteResponse.memberId)))
          .thenReturn(Future(Some(memberToDeleteResponse)))
        when(
          defaultMemberRepository.getMemberByAccountId(
            ArgumentMatchers.eq(account.accountId),
            ArgumentMatchers.eq(project.projectId)
          )
        )
          .thenReturn(Future(Some(memberResponse)))
        when(defaultProjectRepository.getProjectById(ArgumentMatchers.eq(project.projectId)))
          .thenReturn(Future(Some(project)))

        memberService.deleteMember(memberToDeleteResponse.memberId, account).failed.map { exception =>
          exception.isInstanceOf[InsufficientPermissionException] shouldBe true
        }
      }
    }
  }
}
