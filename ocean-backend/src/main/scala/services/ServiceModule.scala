package org.abteilung6.ocean
package services

import utils.RuntimeConfig

trait ServiceModule {
  import com.softwaremill.macwire._
  // We provide a runtimeConfig for the `DirectoryService` here,
  // because this constructor is incompatible with macwire.
  val runtimeConfig: RuntimeConfig = RuntimeConfig.load()

  lazy val directoryService: DirectoryService = wire[DirectoryService]
  lazy val authService: AuthService = wire[AuthService]
}
