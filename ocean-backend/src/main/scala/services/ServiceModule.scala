package org.abteilung6.ocean
package services

import repositories.RepositoryModule

trait ServiceModule extends RepositoryModule {
  import com.softwaremill.macwire._

  lazy val directoryService: DirectoryService = wire[DirectoryService]
  lazy val jwtService: JwtService = wire[JwtService]
  lazy val authService: AuthService = wire[AuthService]
  lazy val emailService: EmailService = wire[EmailService]
  lazy val projectService: ProjectService = wire[ProjectService]
}
