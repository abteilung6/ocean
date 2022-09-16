package org.abteilung6.ocean
package services

trait ServiceModule {
  import com.softwaremill.macwire._

  lazy val authService: AuthService = wire[AuthService]
}
