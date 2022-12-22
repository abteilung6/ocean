package org.abteilung6.ocean
package services

import repositories.{ MemberRepository, ProjectRepository }
import repositories.dto.Account
import repositories.dto.project.{ CreateProjectRequest, MemberState, Project, RoleType }
import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ProjectService(projectRepository: ProjectRepository, memberRepository: MemberRepository) {

  import ProjectService.Exceptions._

  def getProjects(accountId: Long): Future[Seq[Project]] =
    projectRepository.getProjectsByAccountId(accountId)

  def getProject(accountId: Long, projectId: Long): Future[Project] =
    for {
      project <- projectRepository
        .getProjectById(projectId)
        .flatMap {
          case None          => Future.failed(ProjectDoesNotExistException())
          case Some(project) => Future(project)
        }
      _ <- memberRepository
        .getMembersByProjectId(projectId)
        .flatMap {
          case members if !members.exists(_.accountId == accountId) =>
            Future.failed(InsufficientPermissionException("Account is not a member of the project"))
          case members => Future(members)
        }
    } yield project

  def createProject(createProjectRequest: CreateProjectRequest, account: Account): Future[Project] = {
    val project =
      Project(0L, createProjectRequest.name, createProjectRequest.description, Instant.now(), account.accountId)
    projectRepository.addProject(project).recoverWith { case _ => Future.failed(ProjectAlreadyExistsException()) }
  }

  def deleteProject(projectId: Long, account: Account): Future[Int] =
    for {
      _ <- projectRepository
        .getProjectById(projectId)
        .flatMap {
          case Some(project) => Future(project)
          case None          => Future.failed(ProjectDoesNotExistException())
        }
      _ <- memberRepository
        .getMemberByAccountId(account.accountId, projectId)
        .flatMap {
          case Some(memberResponse)
              if memberResponse.state == MemberState.Active && memberResponse.roleType == RoleType.Admin =>
            Future(memberResponse)
          case Some(_) =>
            Future.failed(
              InsufficientPermissionException(
                s"Only active members with role ${RoleType.Admin.entryName} can delete members from project."
              )
            )
          case None => Future.failed(InsufficientPermissionException("You are not a member of the project."))
        }
      _ <- memberRepository.deleteMembersByProjectId(projectId)
      rowsDeleted <- projectRepository.deleteProject(projectId)
    } yield rowsDeleted
}

object ProjectService {
  object Exceptions {
    abstract class ProjectServiceException(message: String) extends Exception(message)

    case class InsufficientPermissionException(message: String = "Insufficient permission")
        extends ProjectServiceException(message)

    case class ProjectDoesNotExistException(message: String = "Project does not exist")
        extends ProjectServiceException(message)

    case class ProjectAlreadyExistsException(message: String = "Project already exists")
        extends ProjectServiceException(message)
  }
}
