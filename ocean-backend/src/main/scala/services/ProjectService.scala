package org.abteilung6.ocean
package services

import repositories.{ MemberRepository, ProjectRepository }
import repositories.dto.Account
import repositories.dto.project.{ CreateProjectRequest, Project }
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
