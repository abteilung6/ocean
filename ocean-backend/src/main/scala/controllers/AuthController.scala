package org.abteilung6.ocean
package controllers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Directives.path
import akka.http.scaladsl.server.Route
import services.AuthService
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import repositories.dto.auth.{ RefreshTokenRequest, SignInRequest }
import repositories.dto.response.ResponseError
import scala.util.{ Failure, Success }

class AuthController(authService: AuthService) extends FailFastCirceSupport {

  import repositories.dto.auth.SignInRequest.Implicits._
  import repositories.dto.auth.AuthResponse.Implicits._
  import repositories.dto.auth.RefreshTokenRequest.Implicits._
  import repositories.dto.response.ResponseError.Implicits._

  private val signIn: Route = path("signin") {
    post {
      entity(as[SignInRequest]) { request =>
        onComplete(authService.authenticate(request.username, request.password)) {
          case Success(authResponse) => complete(authResponse)
          case Failure(AuthService.IncorrectCredentialsException(message)) =>
            complete(StatusCodes.Unauthorized, ResponseError(message))
          case Failure(AuthService.InternalError(message)) =>
            complete(StatusCodes.InternalServerError, ResponseError(message))
        }
      }
    }
  }

  private val refreshToken: Route = path("refresh") {
    post {
      entity(as[RefreshTokenRequest]) { request =>
        authService.refreshTokens(request.refreshToken) match {
          case Some(authResponse) => complete(authResponse)
          case None               => complete(StatusCodes.Unauthorized, ResponseError("Invalid refresh token"))
        }
      }
    }
  }

  val route: Route = pathPrefix("auth") {
    signIn ~ refreshToken
  }
}
