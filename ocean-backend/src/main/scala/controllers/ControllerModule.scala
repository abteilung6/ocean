package org.abteilung6.ocean
package controllers

import akka.http.scaladsl.server.Route
import services.ServiceModule
import utils.RuntimeConfig

class ControllerModule(runtimeConfig: RuntimeConfig) extends ServiceModule {

  import com.softwaremill.macwire._

  lazy val authController: AuthController = wire[AuthController]

  def routes: Route = authController.route
}
