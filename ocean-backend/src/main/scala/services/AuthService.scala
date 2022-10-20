package org.abteilung6.ocean
package services

import repositories.{ Account, AccountRepository }
import services.DirectoryService.UserEntry
import repositories.dto.auth.{ AccessTokenContent, AuthResponse, RefreshTokenContent }
import java.sql.Timestamp
import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{ Failure, Success }

class AuthService(directoryService: DirectoryService, accountRepository: AccountRepository, jwtService: JwtService) {

  import AuthService._

  def authenticate(username: String, password: String): Future[AuthResponse] =
    directoryService.authenticate(username, password) match {
      case Failure(exception) => withErrorMapping(exception)
      case Success(userEntry) =>
        getOrCreateUser(userEntry)
          .flatMap { account =>
            Future.successful(
              jwtService.obtainsTokens(
                AccessTokenContent(account.id.toInt),
                RefreshTokenContent(account.id.toInt),
                Instant.now.getEpochSecond
              )
            )
          }
          .recoverWith { case e: Throwable =>
            withErrorMapping(e)
          }
    }

  def refreshTokens(refreshToken: String): Option[AuthResponse] =
    jwtService.refreshTokens(refreshToken, Instant.now.getEpochSecond)

  private def getOrCreateUser(userEntry: UserEntry): Future[Account] =
    for {
      accountOpt <- accountRepository.getAccountByUsername(userEntry.uid)
      account <- accountOpt match {
        case Some(value) => Future.successful(value)
        case None        => accountRepository.addAccount(mapUserEntryToAccount(userEntry))
      }
    } yield account

  private def mapUserEntryToAccount(userEntry: UserEntry): Account =
    Account(
      0L,
      userEntry.uid,
      userEntry.mail,
      userEntry.givenName,
      userEntry.name,
      userEntry.employeeType,
      Timestamp.from(Instant.now),
      None,
      None
    )

  def withErrorMapping(throwable: Throwable): Future[Nothing] =
    throwable match {
      case _: DirectoryService.Exceptions.AccessDenied  => Future.failed(IncorrectCredentialsException())
      case e: DirectoryService.Exceptions.InternalError => Future.failed(InternalError(e.message))
      case e: Exception                                 => Future.failed(internalError(e))
    }

  private def internalError(throwable: Throwable): InternalError =
    InternalError("Something went wrong :(")
}

object AuthService {
  sealed abstract class AuthServiceException(message: String) extends Exception(message)

  case class IncorrectCredentialsException(message: String = "Incorrect credentials")
      extends AuthServiceException(message)

  final case class InternalError(message: String = "Internal error") extends AuthServiceException(message)
}
