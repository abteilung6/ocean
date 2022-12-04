package org.abteilung6.ocean
package repositories.dto.project

import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import io.circe.{ Decoder, Encoder }

import java.time.Instant

case class Project(
  projectId: Long,
  name: String,
  description: String,
  createdAt: Instant,
  ownerId: Long
)

object Project {
  object Implicits {
    implicit val projectDecoder: Decoder[Project] = deriveDecoder
    implicit val projectEncoder: Encoder[Project] = deriveEncoder
  }
}
