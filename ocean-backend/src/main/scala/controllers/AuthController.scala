package org.abteilung6.ocean
package controllers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import services.{ AuthService, EmailService, JwtService }
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import repositories.dto.auth.{
  AuthResponse,
  RefreshTokenRequest,
  RegisterAccountRequest,
  SignInRequest,
  VerificationTokenContent
}
import repositories.dto.response.ResponseError
import repositories.dto.{ Account, AuthenticatorType }
import utils.RuntimeConfig
import scala.concurrent.Future
import sttp.tapir._
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter
import sttp.tapir.json.circe._
import sttp.tapir.generic.auto._
import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global

class AuthController(
  authService: AuthService,
  emailService: EmailService,
  jwtService: JwtService,
  runtimeConfig: RuntimeConfig
) extends BaseController
    with FailFastCirceSupport {

  import repositories.dto.auth.SignInRequest.Implicits._
  import repositories.dto.auth.AuthResponse.Implicits._
  import repositories.dto.auth.RefreshTokenRequest.Implicits._
  import repositories.dto.auth.RegisterAccountRequest.Implicits._
  import repositories.dto.response.ResponseError.Implicits._
  import repositories.dto.Account.Implicits._

  override val tag: String = "Authentication"

  override val basePath: String = "auth"

  override def endpoints: List[AnyEndpoint] =
    List(signInEndpoint, refreshTokenEndpoint, registerAccountEndpoint, verifyEndpoint)

  override def route: Route =
    AkkaHttpServerInterpreter().toRoute(
      List(
        signInEndpoint.serverLogic((signInLogic _).tupled),
        refreshTokenEndpoint.serverLogic(refreshTokenLogic),
        registerAccountEndpoint.serverLogic(registerAccountLogic),
        verifyEndpoint.serverLogic(verifyLogic)
      )
    )

  val signInEndpoint: PublicEndpoint[(AuthenticatorType, SignInRequest), ResponseError, AuthResponse, Any] =
    endpoint.post
      .tag(tag)
      .description("Sign in with an authenticator")
      .in(this.toRelativeEndpoint("signin"))
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
        authService
          .authenticateWithCredentials(signInRequest)
          .map { authResponse: AuthResponse =>
            Right(authResponse)
          }
          .recover {
            case AuthService.IncorrectCredentialsException(message) =>
              Left(ResponseError(StatusCodes.Unauthorized.intValue, message))
            case AuthService.InternalError(message) =>
              Left(ResponseError(StatusCodes.InternalServerError.intValue, message))
          }
    }

  val refreshTokenEndpoint: PublicEndpoint[RefreshTokenRequest, ResponseError, AuthResponse, Any] =
    endpoint.post
      .tag(tag)
      .in(this.toRelativeEndpoint("refresh"))
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
      .in(this.toRelativeEndpoint("register"))
      .in(jsonBody[RegisterAccountRequest])
      .errorOut(jsonBody[ResponseError])
      .out(jsonBody[Account])

  def registerAccountLogic(registerAccountRequest: RegisterAccountRequest): Future[Either[ResponseError, Account]] =
    authService
      .registerWithCredentials(registerAccountRequest)
      .map { account =>
        sendRegistrationVerificationMail(account)
        Right(account)
      }
      .recover {
        case AuthService.InternalError(message) =>
          Left(ResponseError(StatusCodes.InternalServerError.intValue, message))
        case e: AuthService.AuthServiceException =>
          Left(ResponseError(StatusCodes.BadRequest.intValue, e.getMessage))
      }

  private def sendRegistrationVerificationMail(account: Account): Unit = {
    val token =
      jwtService.encodeVerificationTokenContent(VerificationTokenContent(account.accountId), Instant.now.getEpochSecond)
    val serviceBindingConfig = runtimeConfig.serverBindingConfig
    val verificationUrl =
      this.toAbsoluteURI(
        serviceBindingConfig.interface,
        serviceBindingConfig.port,
        this.toRelativeURI("verify"),
        Map("token" -> token)
      )
    emailService.sendRegistrationVerification(account, verificationUrl)
  }

  val verifyEndpoint: PublicEndpoint[String, ResponseError, Account, Any] =
    endpoint.get
      .tag(tag)
      .description("Verify you account")
      .in(this.toRelativeEndpoint("verify"))
      .in(query[String]("token"))
      .errorOut(jsonBody[ResponseError])
      .out(jsonBody[Account])

  def verifyLogic(token: String): Future[Either[ResponseError, Account]] =
    jwtService.decodeVerificationTokenContent(token, Instant.now.getEpochSecond) match {
      case Some(verificationTokenContent) =>
        authService.verifyAccount(verificationTokenContent.userId) map {
          case Some(account) => Right(account)
          case None          => Left(ResponseError(StatusCodes.BadRequest.intValue, "Account does not exist"))
        }
      case None => Future(Left(ResponseError(StatusCodes.BadRequest.intValue, "Invalid verification token")))
    }
}
