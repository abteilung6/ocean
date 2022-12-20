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
import repositories.dto.project.{ CreateMemberRequest, CreateProjectRequest, Project, RoleType }
import repositories.dto.Account
import services.{ MemberService, ProjectService }
import akka.http.scaladsl.model.StatusCodes
import repositories.dto.response.ResponseError

class ProjectController(
  endpointController: EndpointController,
  projectService: ProjectService,
  memberService: MemberService
) extends BaseController
    with FailFastCirceSupport {

  import repositories.dto.project.Project.Implicits._
  import repositories.dto.project.CreateProjectRequest.Implicits._

  override val tag: String = "Project"

  override val basePath: String = "projects"

  override def route: Route =
    AkkaHttpServerInterpreter().toRoute(List(getProjectEndpoint, getProjectsEndpoint, createProjectEndpoint))

  override def endpoints: List[AnyEndpoint] =
    List(getProjectEndpoint.endpoint, getProjectsEndpoint.endpoint, createProjectEndpoint.endpoint)

  val getProjectsEndpoint: ServerEndpoint.Full[String, Account, Unit, ResponseError, Seq[Project], Any, Future] =
    endpointController.secureEndpointWithUser.get
      .tag(tag)
      .description("List projects")
      .in(basePath)
      .out(jsonBody[Seq[Project]])
      .serverLogic { (account: Account) => _ =>
        projectService
          .getProjects(account.accountId)
          .map(projects => Right(projects))
          .recover { case _ =>
            Left(ResponseError(StatusCodes.InternalServerError.intValue, "Something went wrong"))
          }
      }

  val getProjectEndpoint: ServerEndpoint.Full[String, Account, Long, ResponseError, Project, Any, Future] =
    endpointController.secureEndpointWithUser.get
      .tag(tag)
      .description("Get a project")
      .in(basePath / path[Long]("projectId"))
      .out(jsonBody[Project])
      .serverLogic { account => projectId =>
        projectService
          .getProject(account.accountId, projectId)
          .map(project => Right(project))
          .recover {
            case e: ProjectService.Exceptions.ProjectDoesNotExistException =>
              Left(ResponseError(StatusCodes.NotFound.intValue, e.getMessage))
            case e: ProjectService.Exceptions.InsufficientPermissionException =>
              Left(ResponseError(StatusCodes.Forbidden.intValue, e.getMessage))
            case _ => Left(ResponseError(StatusCodes.InternalServerError.intValue, "Something went wrong"))
          }
      }

  val createProjectEndpoint
    : ServerEndpoint.Full[String, Account, CreateProjectRequest, ResponseError, Project, Any, Future] =
    endpointController.secureEndpointWithUser.post
      .tag(tag)
      .description("Create a project")
      .in(basePath)
      .in(jsonBody[CreateProjectRequest])
      .out(jsonBody[Project])
      .serverLogic { (account: Account) => createProjectRequest =>
        val futureProject = for {
          project <- projectService.createProject(createProjectRequest, account)
          _ <- memberService.createMember(
            CreateMemberRequest(project.projectId, account.accountId, RoleType.Admin),
            account,
            forOwner = true
          )
        } yield project
        futureProject
          .map(project => Right(project))
          .recover {
            case e: ProjectService.Exceptions.ProjectAlreadyExistsException =>
              Left(ResponseError(StatusCodes.BadRequest.intValue, e.message))
            case _ => Left(ResponseError(StatusCodes.InternalServerError.intValue, "Something went wrong"))
          }
      }
}
