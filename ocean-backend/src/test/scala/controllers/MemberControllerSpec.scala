package org.abteilung6.ocean
package controllers

import controllers.endpoints.EndpointController
import controllers.utils.TestEndpointController.createEndpointController
import repositories.dto.Account
import repositories.dto.project.MemberResponse
import repositories.utils.TestMockUtils.{ getMockAccount, getMockCreateMemberRequest, getMockMemberResponse }
import services.{ EmailService, JwtService, MemberService }
import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.model.{ HttpEntity, StatusCodes }
import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import repositories.dto.response.ResponseError
import services.EmailService.Mail
import org.abteilung6.ocean.utils.{ RuntimeConfig, ServerBindingConfig }
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.Future

class MemberControllerSpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with ScalatestRouteTest
    with FailFastCirceSupport
    with BeforeAndAfterEach {

  var defaultAccount: Account = getMockAccount()
  var defaultMemberService: MemberService = mock[MemberService]
  var defaultEmailService: EmailService = mock[EmailService]
  var defaultJwtService: JwtService = mock[JwtService]
  var defaultRuntimeConfig: RuntimeConfig = mock[RuntimeConfig]

  override def beforeEach(): Unit = {
    defaultAccount = getMockAccount()
    defaultMemberService = mock[MemberService]
    defaultEmailService = mock[EmailService]
    defaultJwtService = mock[JwtService]
    defaultRuntimeConfig = mock[RuntimeConfig]
  }

  private def createMemberController(
    endpointController: EndpointController = createEndpointController(account = defaultAccount),
    memberService: MemberService = defaultMemberService,
    emailService: EmailService = defaultEmailService,
    jwtService: JwtService = defaultJwtService,
    runtimeConfig: RuntimeConfig = defaultRuntimeConfig
  ): MemberController = {
    when(runtimeConfig.serverBindingConfig).thenReturn(ServerBindingConfig("localhost", 8080))
    new MemberController(endpointController, memberService, emailService, jwtService, runtimeConfig)
  }

  "create member endpoint" should {
    import repositories.dto.project.CreateMemberRequest.Implicits._
    import repositories.dto.project.MemberResponse.Implicits._
    import repositories.dto.response.ResponseError.Implicits._

    import io.circe.syntax._

    "return the created project" in {
      val memberController =
        createMemberController(memberService = defaultMemberService)
      val createMemberRequest = getMockCreateMemberRequest()
      val httpEntity = HttpEntity(`application/json`, createMemberRequest.asJson.spaces2)
      val memberResponse = getMockMemberResponse()

      when(
        defaultMemberService.createMember(
          ArgumentMatchers.eq(createMemberRequest),
          ArgumentMatchers.eq(defaultAccount),
          ArgumentMatchers.eq(false)
        )
      )
        .thenReturn(Future(memberResponse))
      when(defaultJwtService.encodeMemberVerificationTokenContent(any(), any()))
        .thenReturn("ey..")
      when(defaultEmailService.sendMemberVerification(any(), any()))
        .thenReturn(Mail("from", "to", "subject", "content"))

      Post(memberController.toRelativeURI(), httpEntity) ~> addCredentials(
        OAuth2BearerToken("ey..")
      ) ~> memberController.route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[MemberResponse] shouldBe memberResponse
      }
    }

    List(
      Tuple2(MemberService.Exceptions.InsufficientPermissionException(), StatusCodes.Forbidden),
      Tuple2(MemberService.Exceptions.MemberExistsException(), StatusCodes.BadRequest),
      Tuple2(MemberService.Exceptions.ProjectDoesNotExistException(), StatusCodes.BadRequest),
      Tuple2(new Exception(), StatusCodes.InternalServerError)
    ).foreach { case (exception, statusCode) =>
      s"return ErrorResponse with statusCode ${statusCode} for exception ${exception.toString}" in {
        val memberController =
          createMemberController(memberService = defaultMemberService)
        val createMemberRequest = getMockCreateMemberRequest()
        val httpEntity = HttpEntity(`application/json`, createMemberRequest.asJson.spaces2)

        when(defaultMemberService.createMember(any(), any(), any()))
          .thenReturn(Future.failed(exception))

        Post(memberController.toRelativeURI(), httpEntity) ~> addCredentials(
          OAuth2BearerToken("ey..")
        ) ~> memberController.route ~> check {
          val responseError = responseAs[ResponseError]
          responseError.statusCode shouldBe statusCode.intValue
          status shouldBe StatusCodes.BadRequest
        }
      }
    }
  }
}
