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
import org.abteilung6.ocean.repositories.dto.response.ResponseError

class AuthControllerSpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with ScalatestRouteTest
    with FailFastCirceSupport {

  import repositories.dto.auth.AuthResponse.Implicits._
  import repositories.dto.response.ResponseError.Implicits._

  "signIn" should {
    val username = "username1"
    val password = "password1"
    val signInRequestStr = s"""{"username":"${username}","password":"${password}"}"""
    val httpEntity = HttpEntity(`application/json`, signInRequestStr)

    "return an AuthResponse with access and refresh token" in {
      val authResponse = AuthResponse("accessToken", "refreshToken")
      val authServiceMock: AuthService = mock[AuthService]
      val authController = new AuthController(authServiceMock)

      when(authServiceMock.authenticate(anyString(), anyString()))
        .thenReturn(Future.successful(authResponse))

      Post("/auth/signin", httpEntity) ~> authController.route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[AuthResponse] shouldBe authResponse
      }
    }

    "return Unauthorized status if user does not exist" in {
      val authServiceMock: AuthService = mock[AuthService]
      val authController = new AuthController(authServiceMock)

      when(authServiceMock.authenticate(anyString(), anyString()))
        .thenReturn(Future.failed(AuthService.IncorrectCredentialsException("foo")))

      Post("/auth/signin", httpEntity) ~> authController.route ~> check {
        status shouldBe StatusCodes.Unauthorized
        responseAs[ResponseError] shouldBe ResponseError("foo")
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

      Post("/auth/refresh", httpEntity) ~> authController.route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[AuthResponse] shouldBe authResponse
      }
    }

    "return Unauthorized status if refreshToken is invalid" in {
      val authServiceMock: AuthService = mock[AuthService]
      val authController = new AuthController(authServiceMock)

      when(authServiceMock.refreshTokens(anyString())).thenReturn(None)

      Post("/auth/refresh", httpEntity) ~> authController.route ~> check {
        status shouldBe StatusCodes.Unauthorized
        responseAs[ResponseError] shouldBe ResponseError("Invalid refresh token")
      }
    }
  }
}
