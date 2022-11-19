package org.abteilung6.ocean
package services

import repositories.AccountRepository
import repositories.utils.TestAccountUtils.getDummyAccount
import services.DirectoryService.UserEntry
import repositories.dto.auth.AuthResponse
import org.mockito.ArgumentMatchers.{ any, anyString }
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{ Failure, Success }

class AuthServiceSpec extends AnyWordSpec with Matchers with MockitoSugar {

  val defaultDirectoryServiceMock: DirectoryService = mock[DirectoryService]
  val defaultAccountRepositoryMock: AccountRepository = mock[AccountRepository]
  val defaultJwtServiceMock: JwtService = mock[JwtService]

  private def createAuthService(
    directoryService: DirectoryService = defaultDirectoryServiceMock,
    accountRepository: AccountRepository = defaultAccountRepositoryMock,
    jwtService: JwtService = defaultJwtServiceMock
  ): AuthService =
    new AuthService(directoryService, accountRepository, jwtService)

  "authenticate" should {
    "return an AuthResponse" in {
      val directoryServiceMock = mock[DirectoryService]
      val accountRepositoryMock = mock[AccountRepository]
      val jwtServiceMock = mock[JwtService]
      val authService =
        createAuthService(
          directoryService = directoryServiceMock,
          accountRepository = accountRepositoryMock,
          jwtService = jwtServiceMock
        )
      val userEntry = UserEntry("uid", "name", "givenName", "sn", "mail", "employeeType", "accountExpires")
      val dummyAccount = getDummyAccount(id = 1)
      val authResponseMock = AuthResponse("ey.accessToken", "ey.refreshToken")

      when(directoryServiceMock.authenticate(anyString(), anyString()))
        .thenReturn(Success(userEntry))
      when(accountRepositoryMock.getAccountByUsername(anyString(), any()))
        .thenReturn(Future.successful(Some(dummyAccount)))
      when(jwtServiceMock.obtainsTokens(any(), any(), any()))
        .thenReturn(authResponseMock)

      val futureAuthResponse = authService.authenticateWithDirectory("foo", "bar")
      futureAuthResponse.map { authResponse =>
        authResponse shouldBe authResponseMock
      }
    }

    "return an IncorrectCredentialsException if credentials are incorrect" in {
      val directoryServiceMock = mock[DirectoryService]
      val authService = createAuthService(directoryService = directoryServiceMock)
      when(directoryServiceMock.authenticate(anyString(), anyString()))
        .thenReturn(Failure(DirectoryService.Exceptions.AccessDenied()))

      val futureException = authService.authenticateWithDirectory("foo", "bar")
      futureException.failed.map { exception =>
        exception.isInstanceOf[AuthService.IncorrectCredentialsException] shouldBe true
      }
    }
  }
}
