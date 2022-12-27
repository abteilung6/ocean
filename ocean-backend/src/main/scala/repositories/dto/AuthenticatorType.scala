package org.abteilung6.ocean
package repositories.dto

import enumeratum._
import sttp.tapir.codec.enumeratum.TapirCodecEnumeratum

sealed abstract class AuthenticatorType(override val entryName: String) extends EnumEntry

object AuthenticatorType extends Enum[AuthenticatorType] with TapirCodecEnumeratum with CirceEnum[AuthenticatorType] {

  val values: IndexedSeq[AuthenticatorType] = findValues

  case object Credentials extends AuthenticatorType("credentials")
}
