package org.abteilung6.ocean
package db

import utils.{ FlywayConfig, RuntimeConfig }

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration

trait DBBaseCommand {
  def buildFluentConfiguration(patchFlywayConfig: Option[FlywayConfig] = None): FluentConfiguration = {
    val flywayConfig = patchFlywayConfig match {
      case Some(value) => value
      case None        => RuntimeConfig.load().flywayConfig
    }
    Flyway
      .configure()
      .dataSource(flywayConfig.url, flywayConfig.user, flywayConfig.password)
      .locations(flywayConfig.locations)
      .cleanDisabled(false)
  }
}
