package org.abteilung6.ocean

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import scala.concurrent.{ ExecutionContextExecutor, Future }
import scala.util.{ Failure, Success }
import utils.RuntimeConfig
import controllers.ControllerModule
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors

object OceanApp extends App {

  implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "http-server-system")
  implicit val executionContext: ExecutionContextExecutor = system.executionContext
  val runtimeConfig = RuntimeConfig.load()
  val module = new ControllerModule()
  val openAPIConfig = runtimeConfig.openAPIConfig
  val serverBindingConfig = runtimeConfig.serverBindingConfig
  val swaggerEndpoints =
    SwaggerInterpreter().fromEndpoints[Future](module.endpoints, openAPIConfig.title, openAPIConfig.version)
  val swaggerRoute: Route = AkkaHttpServerInterpreter().toRoute(swaggerEndpoints)

  val routesWithCors = cors() {
    module.routes ~ swaggerRoute
  }

  val bindingFuture = Http()
    .newServerAt(serverBindingConfig.interface, serverBindingConfig.port)
    .bind(routesWithCors)

  bindingFuture.onComplete {
    case Success(binding) =>
      val address = binding.localAddress
      system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
    case Failure(ex) =>
      system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
      system.terminate()
  }
}
