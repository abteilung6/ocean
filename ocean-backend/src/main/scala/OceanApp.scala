package org.abteilung6.ocean

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import scala.concurrent.ExecutionContextExecutor
import scala.util.{ Failure, Success }
import utils.RuntimeConfig
import controllers.ControllerModule

object OceanApp extends App {

  implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "http-server-system")
  implicit val executionContext: ExecutionContextExecutor = system.executionContext
  val serverBindingConfig = RuntimeConfig.load().serverBindingConfig
  val module = new ControllerModule()

  val bindingFuture = Http()
    .newServerAt(serverBindingConfig.interface, serverBindingConfig.port)
    .bind(module.routes)

  bindingFuture.onComplete {
    case Success(binding) =>
      val address = binding.localAddress
      system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
    case Failure(ex) =>
      system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
      system.terminate()
  }
}
