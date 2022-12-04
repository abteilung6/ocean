package org.abteilung6.ocean
package controllers

import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import sttp.tapir._
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter
import sttp.tapir.server.ServerEndpoint
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import endpoints.EndpointController
import repositories.dto.project.{ CreateProjectRequest, Project }
import repositories.dto.Account
import services.ProjectService
import akka.http.scaladsl.model.StatusCodes
import repositories.dto.response.ResponseError

class ProjectController(endpointController: EndpointController, projectService: ProjectService)
    extends BaseController
    with FailFastCirceSupport {

  import repositories.dto.project.Project.Implicits._
  import repositories.dto.project.CreateProjectRequest.Implicits._

  override val tag: String = "Project"

  override val basePath: String = "projects"

  override def route: Route = AkkaHttpServerInterpreter().toRoute(List(createProjectEndpoint))

  override def endpoints: List[AnyEndpoint] = List(createProjectEndpoint.endpoint)

  val createProjectEndpoint
    : ServerEndpoint.Full[String, Account, CreateProjectRequest, ResponseError, Project, Any, Future] =
    endpointController.secureEndpointWithUser.post
      .tag(tag)
      .description("Create a project")
      .in(basePath)
      .in(jsonBody[CreateProjectRequest])
      .out(jsonBody[Project])
      .serverLogic { (account: Account) => createProjectRequest =>
        projectService
          .createProject(createProjectRequest, account)
          .map { project =>
            Right(project)
          }
          .recover { case e: ProjectService.Exceptions.ProjectAlreadyExistsException =>
            Left(ResponseError(StatusCodes.BadRequest.intValue, e.message))
          }
      }
}
