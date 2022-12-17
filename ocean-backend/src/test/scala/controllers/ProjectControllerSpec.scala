package org.abteilung6.ocean
package controllers

import akka.http.scaladsl.testkit.ScalatestRouteTest
import services.{ MemberService, ProjectService }
import akka.http.scaladsl.model.{ HttpEntity, StatusCodes }
import org.mockito.ArgumentMatchers.any
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
import org.mockito.ArgumentMatchers
import org.scalatest.BeforeAndAfterEach

class ProjectControllerSpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with ScalatestRouteTest
    with FailFastCirceSupport
    with BeforeAndAfterEach {

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

  "create project endpoint" should {
    import io.circe.syntax._
    import repositories.dto.project.CreateProjectRequest.Implicits._
    import repositories.dto.project.Project.Implicits._

    "return the created project" in {
      val projectController =
        createProjectController(projectService = defaultProjectService, memberService = defaultMemberService)
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
  }
}
