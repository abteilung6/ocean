package org.abteilung6.ocean
package services

import utils.{ JwtConfig, RuntimeConfig }

import io.circe.Encoder
import repositories.dto.auth.{
  AccessTokenContent,
  AuthContent,
  AuthResponse,
  RefreshTokenContent,
  VerificationTokenContent
}

import org.abteilung6.ocean.repositories.dto.project.MemberVerificationTokenContent
import pdi.jwt.algorithms.JwtHmacAlgorithm
import pdi.jwt.{ JwtAlgorithm, JwtCirce, JwtClaim }

class JwtService(runtimeConfig: RuntimeConfig) {

  import repositories.dto.auth.AuthContent.Implicits._

  val jwtConfig: JwtConfig = runtimeConfig.jwtConfig
  val key: String = jwtConfig.key
  val algorithm: JwtHmacAlgorithm = JwtAlgorithm.HS256
  val accessExpirationTimeInSeconds: Long = jwtConfig.accessExpirationTimeInSeconds
  val refreshExpirationTimeInSeconds: Long = jwtConfig.refreshExpirationTimeInSeconds
  val verificationExpirationTimeInSeconds: Long = jwtConfig.verificationExpirationTimeInSeconds

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

  def encodeVerificationTokenContent(
    verificationTokenContent: VerificationTokenContent,
    currentTimestamp: Long
  ): String = {
    import io.circe.syntax._
    import repositories.dto.auth.VerificationTokenContent.Implicits._

    val claim = JwtClaim(
      content = verificationTokenContent.asJson.toString(),
      expiration = Some(currentTimestamp + verificationExpirationTimeInSeconds),
      issuedAt = Some(currentTimestamp)
    )
    JwtCirce.encode(claim, key, algorithm)
  }

  def decodeVerificationTokenContent(verifyToken: String, currentTimestamp: Long): Option[VerificationTokenContent] = {
    import io.circe.parser.decode
    import repositories.dto.auth.VerificationTokenContent.Implicits._

    val optClaim = JwtCirce.decode(verifyToken, key, Seq(JwtAlgorithm.HS256)).toOption
    optClaim
      .filter(jwtClaim => jwtClaim.expiration.exists(_ >= currentTimestamp))
      .flatMap(jwtClaim => decode[VerificationTokenContent](jwtClaim.content).toOption)
  }

  def encodeMemberVerificationTokenContent(
    memberVerificationTokenContent: MemberVerificationTokenContent,
    currentTimestamp: Long
  ): String = {
    import io.circe.syntax._
    import repositories.dto.project.MemberVerificationTokenContent.Implicits._

    val claim = JwtClaim(
      content = memberVerificationTokenContent.asJson.toString(),
      expiration = Some(currentTimestamp + verificationExpirationTimeInSeconds),
      issuedAt = Some(currentTimestamp)
    )
    JwtCirce.encode(claim, key, algorithm)
  }
}
