package org.abteilung6.ocean
package services

import repositories.AccountRepository
import repositories.dto.auth.{
  AccessTokenContent,
  AuthResponse,
  RefreshTokenContent,
  RegisterAccountRequest,
  SignInRequest
}
import repositories.dto.{ Account, AuthenticatorType }
import utils.{ BCryptUtils, Validator }
import java.time.Instant
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, Future }
import scala.util.{ Failure, Success, Try }

class AuthService(accountRepository: AccountRepository, jwtService: JwtService) {

  import AuthService._

  def authenticateWithCredentials(signInRequest: SignInRequest): Future[AuthResponse] =
    accountRepository.getAccountByEmail(signInRequest.email) flatMap {
      case Some(account) if BCryptUtils.validatePassword(account.passwordHash.get, signInRequest.password).get =>
        Future.successful(
          jwtService.obtainsTokens(
            AccessTokenContent(account.accountId.toInt),
            RefreshTokenContent(account.accountId.toInt),
            Instant.now.getEpochSecond
          )
        )
      case Some(_) => Future.failed(IncorrectCredentialsException())
      case None    => Future.failed(IncorrectCredentialsException())
    }

  def registerWithCredentials(registerAccountRequest: RegisterAccountRequest): Future[Account] = {
    // Email validation
    if (!Validator.validateEmail(registerAccountRequest.email)) {
      return Future.failed(EmailWrongFormatException())
    }

    // Firstname validation
    if (!Validator.isAlphanumeric(registerAccountRequest.firstname)) {
      return Future.failed(FirstnameWrongFormatException("Firstname must be alphanumeric"))
    }
    if (
      !Validator.min(registerAccountRequest.firstname, 2)
      || !Validator.max(registerAccountRequest.firstname, 20)
    ) {
      return Future.failed(FirstnameWrongFormatException("Firstname must be between 2 and 20 characters long"))
    }

    // Lastname validation
    if (!Validator.isAlphanumeric(registerAccountRequest.lastname)) {
      return Future.failed(LastnameWrongFormatException("Lastname must be alphanumeric"))
    }
    if (
      !Validator.min(registerAccountRequest.lastname, 2)
      || !Validator.max(registerAccountRequest.lastname, 20)
    ) {
      return Future.failed(LastnameWrongFormatException("Lastname must be between 2 and 20 characters long"))
    }

    // Password validation
    if (
      !Validator.min(registerAccountRequest.password, 6)
      || !Validator.max(registerAccountRequest.password, 64)
    ) {
      return Future.failed(PasswordWrongFormatException("Password must be between 6 and 64 characters long"))
    }

    val accountWithEmail =
      Await.result(accountRepository.getAccountByEmail(registerAccountRequest.email), Duration(5, TimeUnit.SECONDS))
    if (accountWithEmail.isDefined) {
      return Future.failed(AccountAlreadyExistsException("Account with the same email already exists"))
    }

    mapRegisterAccountRequestToAccount(registerAccountRequest) match {
      case Success(accountToBeCreated) => accountRepository.addAccount(accountToBeCreated)
      // Using exception message would leak data
      case Failure(_) => Future.failed(HashingException())
    }
  }

  def verifyAccount(accountId: Long): Future[Option[Account]] =
    for {
      _ <- accountRepository.verifyAccountById(accountId)
      updatedAccount <- accountRepository.getAccountById(accountId)
    } yield updatedAccount

  def refreshTokens(refreshToken: String): Option[AuthResponse] =
    jwtService.refreshTokens(refreshToken, Instant.now.getEpochSecond)

  private def mapRegisterAccountRequestToAccount(registerAccountRequest: RegisterAccountRequest): Try[Account] =
    BCryptUtils.encryptPassword(registerAccountRequest.password) match {
      case Failure(exception) => Failure(exception)
      case Success(hash) =>
        Success(
          Account(
            0L,
            registerAccountRequest.email,
            registerAccountRequest.firstname,
            registerAccountRequest.lastname,
            registerAccountRequest.company,
            Instant.now(),
            AuthenticatorType.Credentials,
            false,
            Some(hash)
          )
        )
    }
}

object AuthService {

  abstract class AuthServiceException(message: String) extends Exception(message)

  case class IncorrectCredentialsException(message: String = "Incorrect credentials")
      extends AuthServiceException(message)

  case class EmailWrongFormatException(message: String = "Email address has a wrong format")
      extends AuthServiceException(message)

  case class UserWrongFormatException(message: String = "Username has a wrong format")
      extends AuthServiceException(message)

  case class PasswordWrongFormatException(message: String = "Password has a wrong format")
      extends AuthServiceException(message)

  case class FirstnameWrongFormatException(message: String = "Firstname has a wrong format")
      extends AuthServiceException(message)

  case class LastnameWrongFormatException(message: String = "Lastname has a wrong format")
      extends AuthServiceException(message)

  case class AccountAlreadyExistsException(message: String = "Account with the same identity already exists")
      extends AuthServiceException(message)

  case class HashingException(message: String = "Password was more than 71 bytes long")
      extends AuthServiceException(message)

  final case class InternalError(message: String = "Internal error") extends AuthServiceException(message)
}
