package org.abteilung6.ocean
package controllers

import akka.http.scaladsl.testkit.ScalatestRouteTest
import repositories.dto.auth.AccessTokenContent
import services.JwtService
import akka.http.scaladsl.model.StatusCodes
import org.mockito.ArgumentMatchers.{ anyLong, anyString }
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import scala.concurrent.Future
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import akka.http.scaladsl.model.headers.OAuth2BearerToken
import controllers.endpoints.EndpointController
import repositories.AccountRepository
import repositories.dto.Account
import repositories.utils.TestMockUtils.getMockAccount
import org.mockito.ArgumentMatchers

class AccountControllerSpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with ScalatestRouteTest
    with FailFastCirceSupport {

  import repositories.dto.Account.Implicits._

  val defaultAccountRepositoryMock: AccountRepository = mock[AccountRepository]
  val defaultJwtServiceMock: JwtService = mock[JwtService]
  val defaultDummyAccount: Account = getMockAccount(42)

  // We might extract this method in the future,
  // since want to test other endpoints with the same base endpoint.
  private def createEndpointController(
    accountRepository: AccountRepository = defaultAccountRepositoryMock,
    jwtService: JwtService = defaultJwtServiceMock,
    dummyAccount: Account = defaultDummyAccount
  ): EndpointController = {
    when(jwtService.decodeToken(anyString(), anyLong()))
      .thenReturn(Some(AccessTokenContent(42)))
    when(accountRepository.getAccountById(ArgumentMatchers.eq(42L)))
      .thenReturn(Future(Some(dummyAccount)))
    new EndpointController(accountRepository, jwtService)
  }

  "me" should {
    "return the related account" in {
      val endpointController = createEndpointController()
      val accountController = new AccountController(endpointController)

      Get("/api/account/me") ~> addCredentials(OAuth2BearerToken("ey..")) ~> accountController.route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[Account] shouldBe defaultDummyAccount
      }
    }
  }
}
