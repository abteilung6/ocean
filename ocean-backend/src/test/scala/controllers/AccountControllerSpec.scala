package org.abteilung6.ocean
package controllers

import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.model.StatusCodes
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import akka.http.scaladsl.model.headers.OAuth2BearerToken
import repositories.dto.Account
import repositories.utils.TestMockUtils.getMockAccount
import controllers.utils.TestEndpointController.createEndpointController

class AccountControllerSpec
    extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with ScalatestRouteTest
    with FailFastCirceSupport {

  import repositories.dto.Account.Implicits._

  val defaultMockAccount: Account = getMockAccount(42)

  private def createAccountController(account: Account = defaultMockAccount): AccountController = {
    val endpointController = createEndpointController(account = account)
    new AccountController(endpointController)
  }

  "me" should {
    "return the related account" in {
      val accountController = createAccountController()

      Get(accountController.toRelativeURI("me")) ~> addCredentials(
        OAuth2BearerToken("ey..")
      ) ~> accountController.route ~> check {
        status shouldBe StatusCodes.OK
        responseAs[Account] shouldBe defaultMockAccount
      }
    }
  }
}
