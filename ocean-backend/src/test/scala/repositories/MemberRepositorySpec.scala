package org.abteilung6.ocean
package repositories

import repositories.utils.TestDatabase
import repositories.dto.Account
import repositories.dto.project.{ MemberResponse, MemberState, Project, RoleType }
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike
import org.scalatest.{ BeforeAndAfterEach, OptionValues }
import scala.concurrent.Future

class MemberRepositorySpec
    extends TestDatabase("members")
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

  val projectRepository = new ProjectRepository(Some(getPGDatabase))
  val accountRepository = new AccountRepository(Some(getPGDatabase))
  val memberRepository = new MemberRepository(Some(getPGDatabase))

  private def createMember(
    account: Account = getMockAccount(),
    projectName: String = "my-project-1",
    roleType: RoleType = RoleType.Developer,
    memberState: MemberState = MemberState.Active
  ): Future[(Account, Project, MemberResponse)] =
    for {
      addedAccount <- accountRepository.addAccount(account)
      addedProject <- projectRepository.addProject(getMockProject(ownerId = addedAccount.accountId, name = projectName))
      addedMember <- memberRepository.addMember(
        getMockMember(
          projectId = addedProject.projectId,
          accountId = addedAccount.accountId,
          roleType = roleType,
          state = memberState
        )
      )
    } yield Tuple3(addedAccount, addedProject, addedMember)

  "addMember" should {
    "create a member with project and account" in {
      createMember(roleType = RoleType.Viewer, memberState = MemberState.Active).map {
        case (addedAccount, addedProject, addedMember) =>
          addedMember.accountId shouldEqual addedAccount.accountId
          addedMember.accountUsername shouldEqual addedAccount.username
          addedMember.projectId shouldEqual addedProject.projectId
          addedMember.projectName shouldEqual addedProject.name
          addedMember.roleType shouldEqual RoleType.Viewer
          addedMember.state shouldEqual MemberState.Active
      }
    }

    "check unique constraint for account and project" in {
      recoverToSucceededIf[JdbcSQLIntegrityConstraintViolationException] {
        val future = for {
          (addedAccount, addedProject, _) <- createMember()
          memberConstraintViolation <- memberRepository.addMember(
            getMockMember(projectId = addedProject.projectId, accountId = addedAccount.accountId)
          )
        } yield memberConstraintViolation
        future
      }
    }

    "check referential integrity constraint violation for account and project" in {
      recoverToSucceededIf[JdbcSQLIntegrityConstraintViolationException] {
        memberRepository.addMember(getMockMember(accountId = 42))
      }
      recoverToSucceededIf[JdbcSQLIntegrityConstraintViolationException] {
        memberRepository.addMember(getMockMember(projectId = 42))
      }
    }
  }

  "getMemberById" should {
    "return the member with joined project and account" in {
      val future = for {
        (_, _, addedMember) <- createMember()
        receivedMember <- memberRepository.getMemberById(addedMember.memberId)
      } yield Tuple2(addedMember, receivedMember)

      future.map { case (addedMember, receivedMember) =>
        receivedMember.isDefined shouldBe true
        receivedMember.value.memberId shouldEqual addedMember.memberId
        receivedMember.value.projectId shouldEqual addedMember.projectId
        receivedMember.value.accountId shouldEqual addedMember.accountId
      }
    }

    "return option if member does not exist" in {
      val future = for {
        (_, _, addedMember) <- createMember()
        receivedMember <- memberRepository.getMemberById(addedMember.memberId + 1)
      } yield Tuple2(addedMember, receivedMember)

      future.map { case (_, receivedMember) =>
        receivedMember.isEmpty shouldBe true
      }
    }
  }

  "getMembersByProjectId" should {
    "return members with joined project and account" in {
      val future = for {
        (_, _, projectMember) <- createMember(
          account = getMockAccount(username = "username1", email = "email@localhost"),
          projectName = "my-project-1"
        )
        (_, _, differentProjectMember) <- createMember(
          account = getMockAccount(username = "username2", email = "email2@localhost"),
          projectName = "my-project-2"
        )
        receivedMembers <- memberRepository.getMembersByProjectId(projectMember.projectId)
      } yield Tuple3(projectMember, differentProjectMember, receivedMembers)

      future.map { case (addedMember, differentMember, sequences) =>
        val memberIds = sequences.map(_.memberId)
        memberIds should contain(addedMember.memberId)
        memberIds should not contain (differentMember.memberId)
      }
    }
  }

  "acceptMemberById" should {
    "accept member by member id" in {
      val future = for {
        (_, _, invitedMember) <- createMember(memberState = MemberState.Pending)
        acceptedMember <- memberRepository.acceptMemberById(invitedMember.memberId)
      } yield Tuple2(invitedMember, acceptedMember)

      future.map { case (invitedMember, acceptedMember) =>
        acceptedMember.isDefined shouldEqual true
        invitedMember.memberId shouldEqual acceptedMember.value.memberId
        invitedMember.state shouldEqual MemberState.Pending
        acceptedMember.value.state shouldEqual MemberState.Active
      }
    }
  }

  "deleteMemberById" should {
    "delete the member by id" in {
      val future = for {
        (_, _, addedMember) <- createMember()
        rowCount <- memberRepository.deleteMemberById(addedMember.memberId)
        deleteMember <- memberRepository.getMemberById(addedMember.memberId)
      } yield Tuple2(rowCount, deleteMember)

      future.map { case (rowCount, deleteMember) =>
        rowCount shouldBe 1
        deleteMember.isEmpty shouldBe true
      }
    }
  }
}
