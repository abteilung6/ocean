package org.abteilung6.ocean
package services

import utils.{ JwtConfig, RuntimeConfig }
import io.circe.Encoder
import repositories.dto.auth.{ AccessTokenContent, AuthContent, AuthResponse, RefreshTokenContent }
import pdi.jwt.algorithms.JwtHmacAlgorithm
import pdi.jwt.{ JwtAlgorithm, JwtCirce, JwtClaim }

class JwtService(runtimeConfig: RuntimeConfig) {

  import repositories.dto.auth.AuthContent.Implicits._

  val jwtConfig: JwtConfig = runtimeConfig.jwtConfig
  val key: String = jwtConfig.key
  val algorithm: JwtHmacAlgorithm = JwtAlgorithm.HS256
  val accessExpirationTimeInSeconds: Long = jwtConfig.accessExpirationTimeInSeconds
  val refreshExpirationTimeInSeconds: Long = jwtConfig.refreshExpirationTimeInSeconds

  def obtainsTokens(
    accessTokenContent: AccessTokenContent,
    refreshTokenContent: RefreshTokenContent,
    currentTimestamp: Long
  ): AuthResponse = {
    val newAccessToken = getAuthToken(accessTokenContent, currentTimestamp, accessExpirationTimeInSeconds)
    val newRefreshToken = getAuthToken(refreshTokenContent, currentTimestamp, refreshExpirationTimeInSeconds)
    AuthResponse(newAccessToken, newRefreshToken)
  }

  def refreshTokens(refreshToken: String, currentTimestamp: Long): Option[AuthResponse] = {
    import io.circe.parser.decode
    import io.circe.generic.auto._

    val optClaim = JwtCirce.decode(refreshToken, key, Seq(JwtAlgorithm.HS256)).toOption
    optClaim
      .filter(jwtClaim => jwtClaim.expiration.exists(_ >= currentTimestamp))
      .map(jwtClaim => decode[RefreshTokenContent](jwtClaim.content).toOption)
      .collect { case Some(refreshTokenContent) =>
        val accessTokenContent = AccessTokenContent(refreshTokenContent.userId)
        val newAccessToken = getAuthToken(accessTokenContent, currentTimestamp, accessExpirationTimeInSeconds)
        AuthResponse(newAccessToken, refreshToken)
      }
  }

  def getAuthToken[A <: AuthContent](authContent: A, currentTimestamp: Long, lifetime: Long)(implicit
    encoder: Encoder[A]
  ): String = {
    import io.circe.syntax._

    val claim = JwtClaim(
      content = authContent.asJson.toString(),
      expiration = Some(currentTimestamp + lifetime),
      issuedAt = Some(currentTimestamp)
    )
    JwtCirce.encode(claim, key, algorithm)
  }

  def decodeToken(accessToken: String, currentTimestamp: Long): Option[AccessTokenContent] = {
    import io.circe.parser.decode
    import io.circe.generic.auto._

    val optClaim = JwtCirce.decode(accessToken, key, Seq(JwtAlgorithm.HS256)).toOption
    optClaim
      .filter(jwtClaim => jwtClaim.expiration.exists(_ >= currentTimestamp))
      .flatMap(jwtClaim => decode[AccessTokenContent](jwtClaim.content).toOption)
  }
}
