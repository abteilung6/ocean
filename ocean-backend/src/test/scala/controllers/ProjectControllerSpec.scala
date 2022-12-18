package org.abteilung6.ocean
package controllers

import akka.http.scaladsl.testkit.ScalatestRouteTest
import services.{ MemberService, ProjectService }
import akka.http.scaladsl.model.{ HttpEntity, StatusCodes }
import org.mockito.ArgumentMatchers.{ any, anyLong }
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import scala.concurrent.Future
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import akka.http.scaladsl.model.headers.OAuth2BearerToken
import controllers.endpoints.EndpointController
import repositories.dto.Account
import repositories.utils.TestMockUtils.{ getMockAccount, getMockMemberResponse, getMockProject }
import akka.http.scaladsl.model.ContentTypes.`application/json`
import controllers.utils.TestEndpointController.createEndpointController
import repositories.dto.project.{ CreateMemberRequest, CreateProjectRequest, Project, RoleType }
import repositories.dto.response.ResponseError
import org.mockito.ArgumentMatchers
import org.scalatest.BeforeAndAfterEach

class ProjectControllerSpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with ScalatestRouteTest
    with FailFastCirceSupport
    with BeforeAndAfterEach {

  import io.circe.syntax._
  import repositories.dto.project.CreateProjectRequest.Implicits._
  import repositories.dto.project.Project.Implicits._
  import repositories.dto.response.ResponseError.Implicits._

  var defaultAccount: Account = getMockAccount()
  var defaultProjectService: ProjectService = mock[ProjectService]
  var defaultMemberService: MemberService = mock[MemberService]

  override def beforeEach(): Unit = {
    defaultAccount = getMockAccount()
    defaultProjectService = mock[ProjectService]
    defaultMemberService = mock[MemberService]
  }

  private def createProjectController(
    endpointController: EndpointController = createEndpointController(account = defaultAccount),
    projectService: ProjectService = defaultProjectService,
    memberService: MemberService = defaultMemberService
  ): ProjectController =
    new ProjectController(endpointController, projectService, memberService)

  "get projects endpoint" should {
    "return projects" in {
      val projectController = createProjectController()
      val projects = Seq(getMockProject())

      when(defaultProjectService.getProjects(ArgumentMatchers.eq(defaultAccount.accountId)))
        .thenReturn(Future(projects))

      Get(projectController.toRelativeURI()) ~> addCredentials(
        OAuth2BearerToken("ey..")
      ) ~> projectController.route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[Seq[Project]] shouldBe projects
      }
    }
  }

  "get project endpoint" should {
    "return project" in {
      val projectController = createProjectController()
      val project = getMockProject()
      val account = getMockAccount()

      when(
        defaultProjectService.getProject(ArgumentMatchers.eq(account.accountId), ArgumentMatchers.eq(project.projectId))
      )
        .thenReturn(Future(project))

      Get(projectController.toRelativeURI(project.projectId.toString)) ~> addCredentials(
        OAuth2BearerToken("ey..")
      ) ~> projectController.route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[Project] shouldBe project
      }
    }

    List(
      Tuple2(ProjectService.Exceptions.ProjectDoesNotExistException(), StatusCodes.NotFound),
      Tuple2(ProjectService.Exceptions.InsufficientPermissionException(), StatusCodes.Forbidden),
      Tuple2(new Exception(), StatusCodes.InternalServerError)
    ).foreach { case (exception, statusCode) =>
      s"return ErrorResponse with statusCode ${statusCode} for exception ${exception.toString}" in {
        val projectController = createProjectController()

        when(defaultProjectService.getProject(anyLong(), anyLong()))
          .thenReturn(Future.failed(exception))

        Get(projectController.toRelativeURI("0")) ~> addCredentials(
          OAuth2BearerToken("ey..")
        ) ~> projectController.route ~> check {
          val responseError = responseAs[ResponseError]
          responseError.statusCode shouldBe statusCode.intValue
          status shouldBe StatusCodes.BadRequest
        }
      }
    }
  }

  "create project endpoint" should {
    "return the created project" in {
      val projectController = createProjectController()
      val createProjectRequest = CreateProjectRequest("name", "description")
      val httpEntity = HttpEntity(`application/json`, createProjectRequest.asJson.spaces2)
      val mockProject = getMockProject(name = createProjectRequest.name, description = createProjectRequest.description)
      val createMemberRequest = CreateMemberRequest(mockProject.projectId, defaultAccount.accountId, RoleType.Admin)

      when(defaultProjectService.createProject(ArgumentMatchers.eq(createProjectRequest), any()))
        .thenReturn(Future(mockProject))
      when(
        defaultMemberService.createMember(
          ArgumentMatchers.eq(createMemberRequest),
          ArgumentMatchers.eq(defaultAccount),
          ArgumentMatchers.eq(true)
        )
      )
        .thenReturn(Future(getMockMemberResponse()))

      Post(projectController.toRelativeURI(), httpEntity) ~> addCredentials(
        OAuth2BearerToken("ey..")
      ) ~> projectController.route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[Project] shouldBe mockProject
      }
    }

    List(
      Tuple2(ProjectService.Exceptions.ProjectAlreadyExistsException(), StatusCodes.BadRequest),
      Tuple2(new Exception(), StatusCodes.InternalServerError)
    ).foreach { case (exception, statusCode) =>
      s"return ErrorResponse with statusCode ${statusCode} for exception ${exception.toString}" in {
        val projectController = createProjectController()
        val createProjectRequest = CreateProjectRequest("name", "description")
        val httpEntity = HttpEntity(`application/json`, createProjectRequest.asJson.spaces2)

        when(defaultProjectService.createProject(any(), any()))
          .thenReturn(Future.failed(exception))

        Post(projectController.toRelativeURI(), httpEntity) ~> addCredentials(
          OAuth2BearerToken("ey..")
        ) ~> projectController.route ~> check {
          val responseError = responseAs[ResponseError]
          responseError.statusCode shouldBe statusCode.intValue
          status shouldBe StatusCodes.BadRequest
        }
      }
    }
  }
}
