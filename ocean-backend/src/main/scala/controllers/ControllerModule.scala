package org.abteilung6.ocean
package controllers

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import services.ServiceModule
import sttp.tapir.AnyEndpoint
import controllers.endpoints.EndpointController

class ControllerModule extends ServiceModule {

  import com.softwaremill.macwire._

  lazy val authController: AuthController = wire[AuthController]
  lazy val accountController: AccountController = wire[AccountController]
  lazy val projectController: ProjectController = wire[ProjectController]
  lazy val endpointController: EndpointController = wire[EndpointController]

  def routes: Route = authController.route ~ accountController.route ~ projectController.route

  def endpoints: List[AnyEndpoint] =
    authController.endpoints ++ accountController.endpoints ++ projectController.endpoints
}
