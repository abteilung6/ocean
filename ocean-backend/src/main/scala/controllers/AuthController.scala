package org.abteilung6.ocean
package controllers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import services.AuthService
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import repositories.dto.auth.{ AuthResponse, RefreshTokenRequest, RegisterAccountRequest, SignInRequest }
import repositories.dto.response.ResponseError
import repositories.dto.{ Account, AuthenticatorType }
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
  import repositories.dto.auth.RegisterAccountRequest.Implicits._
  import repositories.dto.response.ResponseError.Implicits._
  import repositories.dto.Account.Implicits._

  override val tag: String = "Authentication"

  override val basePath: String = "auth"

  override def endpoints: List[AnyEndpoint] = List(signInEndpoint, refreshTokenEndpoint, registerAccountEndpoint)

  override def route: Route =
    AkkaHttpServerInterpreter().toRoute(
      List(
        signInEndpoint.serverLogic((signInLogic _).tupled),
        refreshTokenEndpoint.serverLogic(refreshTokenLogic),
        registerAccountEndpoint.serverLogic(registerAccountLogic)
      )
    )

  val signInEndpoint: PublicEndpoint[(AuthenticatorType, SignInRequest), ResponseError, AuthResponse, Any] =
    endpoint.post
      .tag(tag)
      .description("Sign in with an authenticator")
      .in(this.withSubEndpoint("signin"))
      .in(query[AuthenticatorType]("authenticator"))
      .in(jsonBody[SignInRequest])
      .errorOut(jsonBody[ResponseError])
      .out(jsonBody[AuthResponse])

  def signInLogic(
    authenticatorType: AuthenticatorType,
    signInRequest: SignInRequest
  ): Future[Either[ResponseError, AuthResponse]] =
    authenticatorType match {
      case AuthenticatorType.Directory =>
        authService
          .authenticateWithDirectory(signInRequest.username, signInRequest.password)
          .map { authResponse: AuthResponse =>
            Right(authResponse)
          }
          .recover {
            case AuthService.IncorrectCredentialsException(message) =>
              Left(ResponseError(StatusCodes.Unauthorized.intValue, message))
            case AuthService.InternalError(message) =>
              Left(ResponseError(StatusCodes.InternalServerError.intValue, message))
          }
      case AuthenticatorType.Credentials =>
        Future(Left(ResponseError(StatusCodes.NotImplemented.intValue, "Not implemented yet")))
    }

  val refreshTokenEndpoint: PublicEndpoint[RefreshTokenRequest, ResponseError, AuthResponse, Any] =
    endpoint.post
      .tag(tag)
      .in(this.withSubEndpoint("refresh"))
      .in(jsonBody[RefreshTokenRequest])
      .errorOut(jsonBody[ResponseError])
      .out(jsonBody[AuthResponse])

  def refreshTokenLogic(refreshTokenRequest: RefreshTokenRequest): Future[Either[ResponseError, AuthResponse]] =
    authService.refreshTokens(refreshTokenRequest.refreshToken) match {
      case Some(authResponse) => Future.successful(Right(authResponse))
      case None => Future.successful(Left(ResponseError(StatusCodes.Unauthorized.intValue, "Invalid refresh token")))
    }

  val registerAccountEndpoint: PublicEndpoint[RegisterAccountRequest, ResponseError, Account, Any] =
    endpoint.post
      .tag(tag)
      .in(this.withSubEndpoint("register"))
      .in(jsonBody[RegisterAccountRequest])
      .errorOut(jsonBody[ResponseError])
      .out(jsonBody[Account])

  def registerAccountLogic(registerAccountRequest: RegisterAccountRequest): Future[Either[ResponseError, Account]] =
    authService
      .registerWithCredentials(registerAccountRequest)
      .map(account => Right(account))
      .recover {
        case AuthService.InternalError(message) =>
          Left(ResponseError(StatusCodes.InternalServerError.intValue, message))
        case e: AuthService.AuthServiceException =>
          Left(ResponseError(StatusCodes.BadRequest.intValue, e.getMessage))
      }
}
