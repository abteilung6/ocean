package org.abteilung6.ocean
package controllers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Directives.path
import akka.http.scaladsl.server.Route
import services.AuthService

class AuthController(authService: AuthService) {

  private val login: Route = path("login") {
    authService.authenticate()
    complete(StatusCodes.OK)
  }

  val route: Route = login
}
