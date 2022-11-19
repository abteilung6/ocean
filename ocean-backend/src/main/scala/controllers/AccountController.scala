package org.abteilung6.ocean
package controllers

import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import sttp.tapir._
import sttp.tapir.json.circe._
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter
import sttp.tapir.server.ServerEndpoint
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import endpoints.EndpointController
import repositories.dto.Account

class AccountController(endpointController: EndpointController) extends BaseController with FailFastCirceSupport {

  override val tag: String = "Account"

  override val basePath: String = "account"

  override def route: Route = AkkaHttpServerInterpreter().toRoute(List(getAccountEndpoint))

  override def endpoints: List[AnyEndpoint] = List(getAccountEndpoint.endpoint)

  import repositories.dto.Account.Implicits._

  val getAccountEndpoint: ServerEndpoint[Any, Future] = endpointController.secureEndpointWithUser.get
    .tag(tag)
    .in(basePath / "me")
    .out(jsonBody[Account])
    .serverLogicSuccess { (account: Account) => _ =>
      Future.successful(account)
    }
}
