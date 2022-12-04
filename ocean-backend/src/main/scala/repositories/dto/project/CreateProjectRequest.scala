package org.abteilung6.ocean
package repositories.dto.project

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }

case class CreateProjectRequest(name: String, description: String)

object CreateProjectRequest {
  object Implicits {
    implicit val createProjectRequestDecoder: Decoder[CreateProjectRequest] = deriveDecoder
    implicit val createProjectRequestEncoder: Encoder[CreateProjectRequest] = deriveEncoder
  }
}
