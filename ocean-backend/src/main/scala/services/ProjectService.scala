package org.abteilung6.ocean
package services

import repositories.ProjectRepository
import repositories.dto.Account
import repositories.dto.project.{ CreateProjectRequest, Project }
import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ProjectService(projectRepository: ProjectRepository) {

  import ProjectService.Exceptions._

  def createProject(createProjectRequest: CreateProjectRequest, account: Account): Future[Project] = {
    val project =
      Project(0L, createProjectRequest.name, createProjectRequest.description, Instant.now(), account.accountId)
    projectRepository.addProject(project).recoverWith { case _ => Future.failed(ProjectAlreadyExistsException()) }
  }
}

object ProjectService {
  object Exceptions {
    abstract class ProjectServiceException(message: String) extends Exception(message)

    case class ProjectAlreadyExistsException(message: String = "Project already exists")
        extends ProjectServiceException(message)
  }
}
