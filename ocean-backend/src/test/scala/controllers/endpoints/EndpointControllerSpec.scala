package org.abteilung6.ocean
package controllers.endpoints

import akka.http.scaladsl.testkit.ScalatestRouteTest
import repositories.dto.auth.AccessTokenContent
import services.JwtService
import org.mockito.ArgumentMatchers.{ anyLong, anyString }
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import scala.concurrent.Future
import repositories.dto.response.ResponseError
import repositories.AccountRepository
import repositories.utils.TestMockUtils.getMockAccount
import org.mockito.ArgumentMatchers

class EndpointControllerSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalatestRouteTest {

  val defaultAccountRepositoryMock: AccountRepository = mock[AccountRepository]
  val defaultJwtServiceMock: JwtService = mock[JwtService]

  private def createEndpointController(
    accountRepository: AccountRepository = defaultAccountRepositoryMock,
    jwtService: JwtService = defaultJwtServiceMock
  ): EndpointController = new EndpointController(accountRepository, jwtService)

  "verifyBearerTokenLogic" should {
    "return the user" in {
      val dummyAccount = getMockAccount(id = 42)
      val accessToken = "ey..."
      val accountRepositoryMock: AccountRepository = mock[AccountRepository]
      val jwtServiceMock: JwtService = mock[JwtService]
      val endpointController =
        createEndpointController(accountRepository = accountRepositoryMock, jwtService = jwtServiceMock)

      when(jwtServiceMock.decodeToken(ArgumentMatchers.eq(accessToken), anyLong()))
        .thenReturn(Some(AccessTokenContent(42)))
      when(accountRepositoryMock.getAccountById(ArgumentMatchers.eq(42L)))
        .thenReturn(Future(Some(dummyAccount)))

      endpointController.verifyBearerTokenLogic(accessToken) map { result =>
        result.isRight shouldBe true
        result shouldBe dummyAccount
      }
    }

    "return a response error if access token is invalid" in {
      val jwtServiceMock: JwtService = mock[JwtService]
      val endpointController =
        createEndpointController(jwtService = jwtServiceMock)

      when(jwtServiceMock.decodeToken(anyString(), anyLong()))
        .thenReturn(None)

      endpointController.verifyBearerTokenLogic("ey...") map { result =>
        result.isLeft shouldBe true
        result shouldBe ResponseError(403, "Invalid token")
      }
    }

    "return a response error if token contains invalid account id" in {
      val accessToken = "ey..."
      val accountRepositoryMock: AccountRepository = mock[AccountRepository]
      val jwtServiceMock: JwtService = mock[JwtService]
      val endpointController =
        createEndpointController(accountRepository = accountRepositoryMock, jwtService = jwtServiceMock)

      when(jwtServiceMock.decodeToken(ArgumentMatchers.eq(accessToken), anyLong()))
        .thenReturn(Some(AccessTokenContent(42)))
      when(accountRepositoryMock.getAccountById(ArgumentMatchers.eq(42L)))
        .thenReturn(Future(None))

      endpointController.verifyBearerTokenLogic(accessToken) map { result =>
        result.isLeft shouldBe true
        result shouldBe ResponseError(403, "Token contains invalid user")
      }
    }
  }
}
