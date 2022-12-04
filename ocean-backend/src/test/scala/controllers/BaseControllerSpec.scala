package org.abteilung6.ocean
package controllers

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import akka.http.scaladsl.server.Route
import sttp.tapir.AnyEndpoint

class BaseControllerSpec extends AnyWordSpec with Matchers with MockitoSugar {

  val defaultBasePath = "entities"
  val defaultTag = "entity"

  private def createChildController(_basePath: String = defaultBasePath, _tag: String = defaultTag) = {
    class ChildController extends BaseController {
      override val basePath: String = _basePath
      override val tag: String = _tag

      override def route: Route = akka.http.scaladsl.server.Directives.get {
        akka.http.scaladsl.server.Directives.complete("OK")
      }

      override def endpoints: List[AnyEndpoint] = List()
    }
    new ChildController()
  }

  "withSubRoute" should {
    "return the route with basePath and parameters" in {
      val childController = createChildController("account")
      childController.toRelativeURI() shouldBe "api/account"
      childController.toRelativeURI("me") shouldBe "api/account/me"
      childController.toRelativeURI("me", parameters = Map("foo" -> "bar")) shouldBe "api/account/me?foo=bar"
    }
  }
}
