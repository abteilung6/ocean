package org.abteilung6.ocean

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import com.typesafe.config.ConfigFactory
import scala.concurrent.ExecutionContextExecutor
import scala.util.{ Failure, Success }
import utils.RuntimeConfig

object HttpServer extends App {

  implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "http-server-system")
  implicit val executionContext: ExecutionContextExecutor = system.executionContext
  val config = ConfigFactory.load()
  val serverBindingConfig = new RuntimeConfig(config).serverBindingConfig

  val route =
    path("") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "hello world"))
      }
    }

  val bindingFuture = Http()
    .newServerAt(serverBindingConfig.interface, serverBindingConfig.port)
    .bind(route)

  bindingFuture.onComplete {
    case Success(binding) =>
      val address = binding.localAddress
      system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
    case Failure(ex) =>
      system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
      system.terminate()
  }
}
