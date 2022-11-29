package org.abteilung6.ocean
package controllers

import akka.http.scaladsl.testkit.ScalatestRouteTest
import repositories.dto.auth.{ AuthResponse, SignInRequest, VerificationTokenContent }
import services.{ AuthService, EmailService, JwtService }
import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.{ HttpEntity, StatusCodes }
import org.mockito.ArgumentMatchers.{ anyLong, anyString }
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import scala.concurrent.Future
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import repositories.dto.response.ResponseError
import repositories.utils.TestMockUtils.{ getMockAccount, getMockRegisterAccountRequest }
import repositories.dto.{ Account, AuthenticatorType }
import services.AuthService.AccountAlreadyExistsException
import utils.{ RuntimeConfig, ServerBindingConfig }
import services.EmailService.Mail
import org.mockito.ArgumentMatchers

class AuthControllerSpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with ScalatestRouteTest
    with FailFastCirceSupport {

  import repositories.dto.Account.Implicits._
  import repositories.dto.auth.AuthResponse.Implicits._
  import repositories.dto.auth.RegisterAccountRequest.Implicits._
  import repositories.dto.response.ResponseError.Implicits._

  val defaultAuthServiceMock: AuthService = mock[AuthService]
  val defaultEmailServiceMock: EmailService = mock[EmailService]
  val defaultJwtServiceMock: JwtService = mock[JwtService]
  val defaultRuntimeConfig: RuntimeConfig = mock[RuntimeConfig]

  private def createAuthController(
    authService: AuthService = defaultAuthServiceMock,
    emailService: EmailService = defaultEmailServiceMock,
    jwtService: JwtService = defaultJwtServiceMock,
    runtimeConfig: RuntimeConfig = defaultRuntimeConfig
  ): AuthController = {
    when(runtimeConfig.serverBindingConfig).thenReturn(ServerBindingConfig("localhost", 8080))
    new AuthController(authService, emailService, jwtService, runtimeConfig)
  }

  "signIn with authenticator type credentials" should {
    import io.circe.syntax._
    import repositories.dto.auth.SignInRequest.Implicits._

    val signInRequest = SignInRequest("username", "password")
    val signInRequestStr = signInRequest.asJson.spaces2
    val httpEntity = HttpEntity(`application/json`, signInRequestStr)

    "return an AuthResponse with access and refresh token" in {
      val authResponse = AuthResponse("accessToken", "refreshToken")
      val authServiceMock: AuthService = mock[AuthService]
      val authController = createAuthController(authService = authServiceMock)

      when(authServiceMock.authenticateWithCredentials(ArgumentMatchers.eq(signInRequest)))
        .thenReturn(Future.successful(authResponse))

      Post(
        authController.withSubRoute("signin", Map("authenticator" -> AuthenticatorType.Credentials.entryName)),
        httpEntity
      ) ~> authController.route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[AuthResponse] shouldBe authResponse
      }
    }

    "return Unauthorized status if user does not exist" in {
      val authServiceMock: AuthService = mock[AuthService]
      val authController = createAuthController(authService = authServiceMock)

      when(authServiceMock.authenticateWithCredentials(ArgumentMatchers.eq(signInRequest)))
        .thenReturn(Future.failed(AuthService.IncorrectCredentialsException("foo")))

      Post(
        authController.withSubRoute("signin", Map("authenticator" -> AuthenticatorType.Credentials.entryName)),
        httpEntity
      ) ~> authController.route ~> check {
        status shouldBe StatusCodes.BadRequest
        responseAs[ResponseError] shouldBe ResponseError(StatusCodes.Unauthorized.intValue, "foo")
      }
    }
  }

  "signIn with authenticator type directory" should {
    val username = "username1"
    val password = "password1"
    val signInRequestStr = s"""{"username":"${username}","password":"${password}"}"""
    val httpEntity = HttpEntity(`application/json`, signInRequestStr)

    "return an AuthResponse with access and refresh token" in {
      val authResponse = AuthResponse("accessToken", "refreshToken")
      val authServiceMock: AuthService = mock[AuthService]
      val authController = createAuthController(authService = authServiceMock)

      when(authServiceMock.authenticateWithDirectory(anyString(), anyString()))
        .thenReturn(Future.successful(authResponse))

      Post(
        authController.withSubRoute("signin", Map("authenticator" -> AuthenticatorType.Directory.entryName)),
        httpEntity
      ) ~> authController.route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[AuthResponse] shouldBe authResponse
      }
    }

    "return Unauthorized status if user does not exist" in {
      val authServiceMock: AuthService = mock[AuthService]
      val authController = createAuthController(authService = authServiceMock)

      when(authServiceMock.authenticateWithDirectory(anyString(), anyString()))
        .thenReturn(Future.failed(AuthService.IncorrectCredentialsException("foo")))

      Post(
        authController.withSubRoute("signin", Map("authenticator" -> AuthenticatorType.Directory.entryName)),
        httpEntity
      ) ~> authController.route ~> check {
        status shouldBe StatusCodes.BadRequest
        responseAs[ResponseError] shouldBe ResponseError(StatusCodes.Unauthorized.intValue, "foo")
      }
    }
  }

  "refresh" should {
    val refreshToken = "refreshToken"
    val refreshRequestStr = s"""{"refreshToken":"${refreshToken}"}"""
    val httpEntity = HttpEntity(`application/json`, refreshRequestStr)

    "return an AuthResponse with updated access token" in {
      val authServiceMock: AuthService = mock[AuthService]
      val authController = createAuthController(authService = authServiceMock)
      val authResponse = AuthResponse("accessToken", "refreshToken")

      when(authServiceMock.refreshTokens(anyString()))
        .thenReturn(Some(authResponse))

      Post(authController.withSubRoute("refresh"), httpEntity) ~> authController.route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[AuthResponse] shouldBe authResponse
      }
    }

    "return Unauthorized status if refreshToken is invalid" in {
      val authServiceMock: AuthService = mock[AuthService]
      val authController = createAuthController(authService = authServiceMock)

      when(authServiceMock.refreshTokens(anyString())).thenReturn(None)

      Post(authController.withSubRoute("refresh"), httpEntity) ~> authController.route ~> check {
        status shouldBe StatusCodes.BadRequest
        responseAs[ResponseError] shouldBe ResponseError(StatusCodes.Unauthorized.intValue, "Invalid refresh token")
      }
    }
  }

  "register with authenticator type credentials" should {
    import io.circe.syntax._

    "return the created account" in {
      val authServiceMock: AuthService = mock[AuthService]
      val jwtService: JwtService = mock[JwtService]
      val emailService: EmailService = mock[EmailService]
      val authController =
        createAuthController(authService = authServiceMock, jwtService = jwtService, emailService = emailService)
      val account = getMockAccount()
      val registerAccountRequest = getMockRegisterAccountRequest()
      val verificationToken = "ey.abc.def"

      when(authServiceMock.registerWithCredentials(ArgumentMatchers.eq(registerAccountRequest)))
        .thenReturn(Future(account))
      when(
        jwtService.encodeVerificationTokenContent(ArgumentMatchers.eq(VerificationTokenContent(account.id)), anyLong())
      )
        .thenReturn(verificationToken)
      when(emailService.sendRegistrationVerification(ArgumentMatchers.eq(account), anyString()))
        .thenReturn(Mail("from", "to", "subject", "content"))

      Post(
        authController.withSubRoute("register"),
        HttpEntity(`application/json`, registerAccountRequest.asJson.spaces2)
      ) ~> authController.route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[Account] shouldBe account
      }
    }

    "return BadRequest status if registration is invalid" in {
      val authServiceMock: AuthService = mock[AuthService]
      val authController = createAuthController(authService = authServiceMock)
      val registerAccountRequest = getMockRegisterAccountRequest(email = "duplicate@duplicate.com")

      when(authServiceMock.registerWithCredentials(ArgumentMatchers.eq(registerAccountRequest)))
        .thenReturn(Future.failed(AccountAlreadyExistsException("Account with the same email already exists")))

      Post(
        authController.withSubRoute("register"),
        HttpEntity(`application/json`, registerAccountRequest.asJson.spaces2)
      ) ~> authController.route ~> check {
        status shouldBe StatusCodes.BadRequest
        responseAs[ResponseError] shouldBe ResponseError(
          StatusCodes.BadRequest.intValue,
          "Account with the same email already exists"
        )
      }
    }
  }

  "verify" should {
    "return account after account is verified" in {
      val authServiceMock: AuthService = mock[AuthService]
      val jwtServiceMock: JwtService = mock[JwtService]
      val authController = createAuthController(authService = authServiceMock, jwtService = jwtServiceMock)
      val unverifiedAccount = getMockAccount(id = 2L, verified = false)
      val verifiedAccount = getMockAccount(id = 2L, verified = true)
      val verificationTokenContent = VerificationTokenContent(unverifiedAccount.id)
      val token = "ey.abc.def"

      when(jwtServiceMock.decodeVerificationTokenContent(ArgumentMatchers.eq(token), anyLong()))
        .thenReturn(Some(verificationTokenContent))
      when(authServiceMock.verifyAccount(ArgumentMatchers.eq(unverifiedAccount.id)))
        .thenReturn(Future(Some(verifiedAccount)))

      Get(authController.withSubRoute("verify", parameters = Map("token" -> token))) ~>
        authController.route ~> check {
          status shouldBe StatusCodes.OK
        }
    }
  }
}
