package org.abteilung6.ocean
package controllers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import services.AuthService
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import repositories.dto.auth.{ AuthResponse, RefreshTokenRequest, SignInRequest }
import repositories.dto.response.ResponseError
import scala.concurrent.Future
import sttp.tapir._
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import scala.concurrent.ExecutionContext.Implicits.global

class AuthController(authService: AuthService) extends BaseController with FailFastCirceSupport {

  import repositories.dto.auth.SignInRequest.Implicits._
  import repositories.dto.auth.AuthResponse.Implicits._
  import repositories.dto.auth.RefreshTokenRequest.Implicits._
  import repositories.dto.response.ResponseError.Implicits._

  override val basePath: String = "auth"

  val signInEndpoint: PublicEndpoint[SignInRequest, ResponseError, AuthResponse, Any] =
    endpoint.post
      .in(basePath / "signin")
      .in(jsonBody[SignInRequest])
      .errorOut(jsonBody[ResponseError])
      .out(jsonBody[AuthResponse])

  def signInLogic(signInRequest: SignInRequest): Future[Either[ResponseError, AuthResponse]] =
    authService
      .authenticate(signInRequest.username, signInRequest.password)
      .map { authResponse: AuthResponse =>
        Right(authResponse)
      }
      .recover {
        case AuthService.IncorrectCredentialsException(message) =>
          Left(ResponseError(StatusCodes.Unauthorized.intValue, message))
        case AuthService.InternalError(message) =>
          Left(ResponseError(StatusCodes.InternalServerError.intValue, message))
      }

  val refreshTokenEndpoint: PublicEndpoint[RefreshTokenRequest, ResponseError, AuthResponse, Any] =
    endpoint.post
      .in(basePath / "refresh")
      .in(jsonBody[RefreshTokenRequest])
      .errorOut(jsonBody[ResponseError])
      .out(jsonBody[AuthResponse])

  def refreshTokenLogic(refreshTokenRequest: RefreshTokenRequest): Future[Either[ResponseError, AuthResponse]] =
    authService.refreshTokens(refreshTokenRequest.refreshToken) match {
      case Some(authResponse) => Future.successful(Right(authResponse))
      case None => Future.successful(Left(ResponseError(StatusCodes.Unauthorized.intValue, "Invalid refresh token")))
    }

  val endpoints: List[AnyEndpoint] = List(signInEndpoint, refreshTokenEndpoint)

  val route: Route =
    AkkaHttpServerInterpreter().toRoute(
      List(signInEndpoint.serverLogic(signInLogic), refreshTokenEndpoint.serverLogic(refreshTokenLogic))
    )
}
