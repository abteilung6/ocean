package org.abteilung6.ocean
package utils

import com.github.t3hnar.bcrypt._
import scala.util.Try

object BCryptUtils {

  def encryptPassword(plainPassword: String): Try[String] =
    plainPassword.bcryptSafeBounded

  def validatePassword(hash: String, plainPassword: String): Try[Boolean] =
    plainPassword.isBcryptedSafeBounded(hash)
}
