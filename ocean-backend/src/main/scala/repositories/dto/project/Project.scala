package org.abteilung6.ocean
package repositories.dto.project

import java.time.Instant

case class Project(
  projectId: Long,
  name: String,
  description: String,
  createdAt: Instant,
  ownerId: Long
)
