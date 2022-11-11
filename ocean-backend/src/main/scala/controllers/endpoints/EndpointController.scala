package org.abteilung6.ocean
package controllers.endpoints

import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import sttp.tapir.{ auth, endpoint }
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.PartialServerEndpoint
import sttp.tapir.generic.auto._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import repositories.dto.response.ResponseError.Implicits._
import repositories.dto.response.ResponseError
import repositories.AccountRepository
import services.JwtService
import repositories.dto.Account
import java.time.Instant

class EndpointController(accountRepository: AccountRepository, jwtService: JwtService) extends FailFastCirceSupport {

  def verifyBearerTokenLogic(accessToken: String): Future[Either[ResponseError, Account]] =
    jwtService.decodeToken(accessToken, Instant.now.getEpochSecond) match {
      case Some(accessTokenContent) =>
        accountRepository.getAccountById(accessTokenContent.userId.toLong).map {
          case Some(account) => Right(account)
          case None          => Left(ResponseError(403, "Token contains invalid user"))
        }
      case None => Future(Left(ResponseError(403, "Invalid token")))
    }

  val secureEndpointWithUser: PartialServerEndpoint[String, Account, Unit, ResponseError, Unit, Any, Future] =
    endpoint
      .in("api")
      .securityIn(auth.bearer[String]())
      .errorOut(jsonBody[ResponseError])
      .serverSecurityLogic(verifyBearerTokenLogic)
}
