package org.abteilung6.ocean
package services

import repositories.MemberRepository
import repositories.ProjectRepository
import repositories.dto.Account
import repositories.dto.project.{ CreateMemberRequest, Member, MemberResponse, MemberState, RoleType }
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration

class MemberService(memberRepository: MemberRepository, projectRepository: ProjectRepository) {

  import MemberService.Exceptions._

  def createMember(
    createMemberRequest: CreateMemberRequest,
    account: Account,
    forOwner: Boolean = false
  ): Future[MemberResponse] = {
    val projectOpt =
      Await.result(projectRepository.getProjectById(createMemberRequest.projectId), Duration(5, TimeUnit.SECONDS))
    val invitorOpt =
      Await.result(memberRepository.getMemberByAccountId(account.accountId), Duration(5, TimeUnit.SECONDS))
    val invitedOpt =
      Await.result(memberRepository.getMemberByAccountId(createMemberRequest.accountId), Duration(5, TimeUnit.SECONDS))

    if (projectOpt.isEmpty) {
      return Future.failed(ProjectDoesNotExistException())
    }

    // Project creation must add the owner initially as a member without any member permission,
    // therefore we only check if the account is the project owner.
    if (forOwner) {
      if (projectOpt.get.ownerId != account.accountId) {
        return Future.failed(InsufficientPermissionException("You are not owner of this project."))
      } else if (invitedOpt.isDefined) {
        return Future.failed(MemberExistsException("Owner is already a project member."))
      } else {
        return memberRepository.addMember(Member.fromCreateMemberRequest(createMemberRequest))
      }
    }

    // Acting as project member
    if (invitorOpt.isEmpty) {
      Future.failed(InsufficientPermissionException("You are not a member of this project."))
    } else if (invitorOpt.get.roleType != RoleType.Admin) {
      Future.failed(
        InsufficientPermissionException(
          s"Only members with role ${RoleType.Admin.entryName} can invite new members to project."
        )
      )
    } else if (invitedOpt.isDefined && invitedOpt.get.state == MemberState.Active) {
      Future.failed(MemberExistsException("Account is already member of this project."))
    } else if (invitedOpt.isDefined && invitedOpt.get.state == MemberState.Pending) {
      Future(invitedOpt.get)
    } else {
      memberRepository.addMember(Member.fromCreateMemberRequest(createMemberRequest))
    }
  }
}

object MemberService {
  object Exceptions {
    abstract class MemberServiceException(message: String) extends Exception(message)

    case class InsufficientPermissionException(message: String = "Insufficient permission")
        extends MemberServiceException(message)

    case class MemberExistsException(message: String = "Member already exists") extends MemberServiceException(message)

    case class ProjectDoesNotExistException(message: String = "Project does not exists")
        extends MemberServiceException(message)
  }
}
