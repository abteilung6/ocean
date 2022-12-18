package org.abteilung6.ocean
package repositories.dto.project

import enumeratum.{ CirceEnum, Enum, EnumEntry }
import sttp.tapir.codec.enumeratum.TapirCodecEnumeratum

sealed abstract class RoleType(override val entryName: String) extends EnumEntry

/**
 * Specifies the role for a member.
 *
 * For now we use a simple enum [[RoleType]] instead of adding complex entity relationships.
 */
object RoleType extends Enum[RoleType] with TapirCodecEnumeratum with CirceEnum[RoleType] {
  override def values: IndexedSeq[RoleType] = findValues

  /**
   * Permissions
   *   - all, write, read
   */
  case object Admin extends RoleType("admin")

  /**
   * Permissions
   *   - write, read
   */
  case object Developer extends RoleType("developer")

  /**
   * Permissions
   *   - read
   */
  case object Viewer extends RoleType("viewer")
}
