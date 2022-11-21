package org.abteilung6.ocean
package controllers

import akka.http.scaladsl.testkit.ScalatestRouteTest
import repositories.dto.auth.AuthResponse
import services.AuthService
import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.{ HttpEntity, StatusCodes }
import org.mockito.ArgumentMatchers.anyString
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

  "signIn with authenticator type directory" should {
    val username = "username1"
    val password = "password1"
    val signInRequestStr = s"""{"username":"${username}","password":"${password}"}"""
    val httpEntity = HttpEntity(`application/json`, signInRequestStr)

    "return an AuthResponse with access and refresh token" in {
      val authResponse = AuthResponse("accessToken", "refreshToken")
      val authServiceMock: AuthService = mock[AuthService]
      val authController = new AuthController(authServiceMock)

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
      val authController = new AuthController(authServiceMock)

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
      val authController = new AuthController(authServiceMock)
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
      val authController = new AuthController(authServiceMock)

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
      val authController = new AuthController(authServiceMock)
      val account = getMockAccount()
      val registerAccountRequest = getMockRegisterAccountRequest()

      when(authServiceMock.registerWithCredentials(ArgumentMatchers.eq(registerAccountRequest)))
        .thenReturn(Future(account))

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
      val authController = new AuthController(authServiceMock)
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
}
