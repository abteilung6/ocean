package org.abteilung6.ocean
package db

import utils.FlywayConfig

import org.flywaydb.core.api.output.{ CleanResult, MigrateResult }

object DBMigrateCommand extends App with DBBaseCommand {

  /**
   * Migrates the schema to the latest version.
   *
   * [[https://flywaydb.org/documentation/command/migrate Migrate command]]
   */
  def migrate(patchFlywayConfig: Option[FlywayConfig] = None): MigrateResult = {
    val flyway = buildFluentConfiguration(patchFlywayConfig).load()
    flyway.migrate()
  }

  def clean(patchFlywayConfig: Option[FlywayConfig] = None): CleanResult = {
    val flyway = buildFluentConfiguration(patchFlywayConfig).load()
    flyway.clean()
  }

  migrate()
}
