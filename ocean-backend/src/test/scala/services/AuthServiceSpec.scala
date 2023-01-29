package org.abteilung6.ocean
package services

import repositories.AccountRepository
import repositories.utils.TestMockUtils.{ getMockAccount, getMockRegisterAccountRequest }
import repositories.dto.auth.{ AuthResponse, SignInRequest }
import repositories.dto.AuthenticatorType
import utils.BCryptUtils
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.{ any, anyString }
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthServiceSpec extends AnyWordSpec with Matchers with MockitoSugar {

  val defaultAccountRepositoryMock: AccountRepository = mock[AccountRepository]
  val defaultJwtServiceMock: JwtService = mock[JwtService]

  private def createAuthService(
    accountRepository: AccountRepository = defaultAccountRepositoryMock,
    jwtService: JwtService = defaultJwtServiceMock
  ): AuthService =
    new AuthService(accountRepository, jwtService)

  "authenticateWithCredentials" should {
    "return an AuthResponse" in {
      val plainPassword = "foo"
      val passwordHash = BCryptUtils.encryptPassword(plainPassword).get
      val accountRepositoryMock = mock[AccountRepository]
      val jwtServiceMock = mock[JwtService]
      val authService = createAuthService(accountRepository = accountRepositoryMock, jwtService = jwtServiceMock)
      val mockAccount = getMockAccount(passwordHash = Some(passwordHash))
      val mockAuthResponse = AuthResponse("ey.accessToken", "ey.refreshToken")

      when(accountRepositoryMock.getAccountByEmail(ArgumentMatchers.eq(mockAccount.email)))
        .thenReturn(Future.successful(Some(mockAccount)))
      when(jwtServiceMock.obtainsTokens(any(), any(), any()))
        .thenReturn(mockAuthResponse)

      authService.authenticateWithCredentials(SignInRequest(mockAccount.email, plainPassword)).map { authResponse =>
        authResponse shouldBe mockAuthResponse
      }
    }
  }

  "registerWithCredentials" should {
    "return the created account" in {
      val accountRepositoryMock = mock[AccountRepository]
      val authService = createAuthService(accountRepository = accountRepositoryMock)
      val registerAccountRequest = getMockRegisterAccountRequest()
      val mockAccount = getMockAccount(accountId = 1)

      when(accountRepositoryMock.addAccount(any()))
        .thenReturn(Future(mockAccount))
      when(accountRepositoryMock.getAccountByEmail(ArgumentMatchers.eq(registerAccountRequest.email)))
        .thenReturn(Future(None))
      when(
        accountRepositoryMock.getAccountByEmail(ArgumentMatchers.eq(registerAccountRequest.email))
      ).thenReturn(Future(None))

      authService.registerWithCredentials(registerAccountRequest).map { account =>
        account shouldBe mockAccount
      }
    }

    "return an AuthServiceException if validation failed" in {
      val accountRepositoryMock = mock[AccountRepository]
      val authService = createAuthService(accountRepository = accountRepositoryMock)
      authService
        .registerWithCredentials(getMockRegisterAccountRequest(email = "!"))
        .failed
        .map { exception =>
          exception shouldBe an[AuthService.EmailWrongFormatException]
        }
    }
  }
}
