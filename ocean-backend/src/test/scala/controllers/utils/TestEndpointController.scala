package org.abteilung6.ocean
package controllers.utils

import controllers.endpoints.EndpointController
import repositories.AccountRepository
import repositories.dto.Account
import repositories.dto.auth.AccessTokenContent
import repositories.utils.TestMockUtils.getMockAccount
import services.JwtService

import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.{ anyLong, anyString }
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object TestEndpointController {
  val defaultAccountRepositoryMock: AccountRepository = mock[AccountRepository]
  val defaultJwtServiceMock: JwtService = mock[JwtService]
  val defaultMockAccount: Account = getMockAccount(42)
  val defaultAccessTokenContent: AccessTokenContent = AccessTokenContent(defaultMockAccount.id.toInt)

  def createEndpointController(
    accountRepository: AccountRepository = defaultAccountRepositoryMock,
    jwtService: JwtService = defaultJwtServiceMock,
    account: Account = defaultMockAccount,
    accessTokenContent: AccessTokenContent = defaultAccessTokenContent
  ): EndpointController = {
    when(jwtService.decodeToken(anyString(), anyLong()))
      .thenReturn(Some(accessTokenContent))
    when(accountRepository.getAccountById(anyLong()))
      .thenReturn(Future(Some(account)))
    new EndpointController(accountRepository, jwtService)
  }
}
