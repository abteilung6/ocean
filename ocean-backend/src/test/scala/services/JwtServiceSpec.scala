package org.abteilung6.ocean
package services

import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import utils.{ JwtConfig, RuntimeConfig }
import repositories.dto.auth.{ AccessTokenContent, RefreshTokenContent }
import org.scalatest.TryValues
import pdi.jwt.JwtCirce
import java.time.Instant

class JwtServiceSpec extends AnyWordSpec with Matchers with MockitoSugar with TryValues {

  private def getJwtService: JwtService = {
    val runtimeConfigMock = mock[RuntimeConfig]
    when(runtimeConfigMock.jwtConfig).thenReturn(JwtConfig("mockKey", 1_000, 10_000, 2_000))
    new JwtService(runtimeConfigMock)
  }

  "obtainsTokens" should {
    val jwtService = getJwtService
    val now = Instant.now.getEpochSecond

    "return an AuthResponse with access and refresh token" in {
      val authResponse = jwtService.obtainsTokens(AccessTokenContent(1), RefreshTokenContent(1), now)
      val accessTokenClaims = JwtCirce.decode(authResponse.accessToken, jwtService.key, Seq(jwtService.algorithm))
      val refreshTokenClaims = JwtCirce.decode(authResponse.refreshToken, jwtService.key, Seq(jwtService.algorithm))
      accessTokenClaims.success.value.expiration shouldBe Some(now + jwtService.accessExpirationTimeInSeconds)
      accessTokenClaims.success.value.issuedAt shouldBe Some(now)
      refreshTokenClaims.success.value.expiration shouldBe Some(now + jwtService.refreshExpirationTimeInSeconds)
      refreshTokenClaims.success.value.issuedAt shouldBe Some(now)
    }
  }

  "refreshTokens" should {
    val jwtService = getJwtService

    "return a refreshed access token" in {
      val now = Instant.now.getEpochSecond
      val refreshToken = jwtService.obtainsTokens(AccessTokenContent(1), RefreshTokenContent(1), now).refreshToken
      val authResponse = jwtService.refreshTokens(refreshToken, now)
      val accessTokenClaims = JwtCirce.decode(authResponse.get.accessToken, jwtService.key, Seq(jwtService.algorithm))
      accessTokenClaims.success.value.expiration shouldBe Some(now + jwtService.accessExpirationTimeInSeconds)
      accessTokenClaims.success.value.issuedAt shouldBe Some(now)
    }

    "avoid the refresh if refresh token is expired" in {
      val now = Instant.now.getEpochSecond
      val overdue = Instant.now.getEpochSecond - jwtService.refreshExpirationTimeInSeconds
      val oldRefreshToken =
        jwtService.obtainsTokens(AccessTokenContent(1), RefreshTokenContent(1), overdue).refreshToken
      val authResponse = jwtService.refreshTokens(oldRefreshToken, now)
      authResponse shouldBe None
    }
  }

  "decodeToken" should {
    val jwtService = getJwtService

    "return access token content" in {
      val accessTokenContent = AccessTokenContent(42)
      val now = Instant.now.getEpochSecond
      val accessToken = jwtService.obtainsTokens(accessTokenContent, RefreshTokenContent(42), now).accessToken

      jwtService.decodeToken(accessToken, now) shouldBe Some(accessTokenContent)
    }

    "return None if access token is invalid" in {
      val overdue = Instant.now.getEpochSecond - jwtService.refreshExpirationTimeInSeconds
      val oldAccessToken =
        jwtService.obtainsTokens(AccessTokenContent(42), RefreshTokenContent(42), overdue).accessToken

      jwtService.decodeToken(oldAccessToken, overdue) shouldBe None
    }
  }
}
