package org.abteilung6.ocean
package repositories.utils

import db.DBMigrateCommand
import utils.FlywayConfig
import org.flywaydb.core.api.output.MigrateResult
import java.sql.{ Connection, DriverManager }

class TestDatabase(databaseName: String = "defaultdb") {

  import TestDatabase._

  var connection: Connection = _
  val connectionString = s"jdbc:h2:mem:${databaseName};MODE=PostgreSQL;DATABASE_TO_LOWER=True"
  val flywayConfig: FlywayConfig = FlywayConfig(connectionString, "", "", "classpath:migrations")

  def deploy(): MigrateResult = {
    this.checkConnectionOrFail()
    connection = DriverManager.getConnection(connectionString)
    DBMigrateCommand.migrate(Some(flywayConfig))
  }

  def reset(): MigrateResult = {
    DBMigrateCommand.clean(Some(flywayConfig))
    DBMigrateCommand.migrate(Some(flywayConfig))
  }

  def teardown(): Unit = {
    connection.close()
    connection = null
  }

  private def checkConnectionOrFail(): Unit =
    if (connection != null && !connection.isClosed) {
      throw AlreadyRunningException()
    }
}

object TestDatabase {
  abstract class TestDatabaseException(message: String) extends RuntimeException(message)

  final case class AlreadyRunningException(message: String = "Database is already running")
      extends TestDatabaseException(message)
}
