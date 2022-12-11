package org.abteilung6.ocean
package repositories.dto.project

import enumeratum.{ CirceEnum, Enum, EnumEntry }
import sttp.tapir.codec.enumeratum.TapirCodecEnumeratum

sealed abstract class MemberState(override val entryName: String) extends EnumEntry

object MemberState extends Enum[MemberState] with TapirCodecEnumeratum with CirceEnum[MemberState] {
  override def values: IndexedSeq[MemberState] = findValues

  /**
   * Account is an active project member, because he accepted the invitation.
   */
  case object Active extends MemberState("active")

  /**
   * Account is not an active project member, because he did not accepted the invitation.
   */
  case object Pending extends MemberState("pending")
}
