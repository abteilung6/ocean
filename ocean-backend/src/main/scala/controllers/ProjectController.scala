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
        val futureProject = for {
          project <- projectService.createProject(createProjectRequest, account)
          _ <- memberService.createMember(
            CreateMemberRequest(project.projectId, account.accountId, RoleType.Admin),
            account,
            forOwner = true
          )
        } yield project
        futureProject.map(project => Right(project)).recover {
          case e: ProjectService.Exceptions.ProjectAlreadyExistsException =>
            Left(ResponseError(StatusCodes.BadRequest.intValue, e.message))
          case _ => Left(ResponseError(StatusCodes.InternalServerError.intValue, "Something went wrong"))
        }
      }
}
