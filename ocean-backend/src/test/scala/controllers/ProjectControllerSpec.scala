package org.abteilung6.ocean
package controllers

import akka.http.scaladsl.testkit.ScalatestRouteTest
import services.ProjectService
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
import repositories.utils.TestMockUtils.{ getMockAccount, getMockProject }
import akka.http.scaladsl.model.ContentTypes.`application/json`
import controllers.utils.TestEndpointController.createEndpointController
import repositories.dto.project.{ CreateProjectRequest, Project }
import org.mockito.ArgumentMatchers

class ProjectControllerSpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with ScalatestRouteTest
    with FailFastCirceSupport {

  val defaultAccount: Account = getMockAccount()
  val defaultProjectService: ProjectService = mock[ProjectService]

  private def createProjectController(
    endpointController: EndpointController = createEndpointController(account = defaultAccount),
    projectService: ProjectService = defaultProjectService
  ): ProjectController =
    new ProjectController(endpointController, projectService)

  "create project endpoint" should {
    import io.circe.syntax._
    import repositories.dto.project.CreateProjectRequest.Implicits._
    import repositories.dto.project.Project.Implicits._

    "return the created project" in {
      val projectServiceMock = mock[ProjectService]
      val projectController = createProjectController(projectService = projectServiceMock)
      val createProjectRequest = CreateProjectRequest("name", "description")
      val httpEntity = HttpEntity(`application/json`, createProjectRequest.asJson.spaces2)
      val mockProject = getMockProject(name = createProjectRequest.name, description = createProjectRequest.description)

      when(projectServiceMock.createProject(ArgumentMatchers.eq(createProjectRequest), any()))
        .thenReturn(Future(mockProject))

      Post(projectController.toRelativeURI(), httpEntity) ~> addCredentials(
        OAuth2BearerToken("ey..")
      ) ~> projectController.route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[Project] shouldBe mockProject
      }
    }
  }
}
