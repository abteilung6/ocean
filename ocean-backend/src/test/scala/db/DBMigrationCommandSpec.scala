package org.abteilung6.ocean
package db

import utils.FlywayConfig
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import java.sql.{ Connection, DriverManager }

class DBMigrationCommandSpec extends AnyWordSpec with Matchers {

  val mockDatabase = "MOCKDB"

  "migrate" should {
    "execute all migrations" in {
      val _: Connection = DriverManager.getConnection("jdbc:h2:mem:play;MODE=PostgreSQL")
      val mockFlywayConfig =
        Some(FlywayConfig(s"jdbc:h2:mem:${mockDatabase};MODE=PostgreSQL", "", "", "classpath:migrations"))
      val result = DBMigrateCommand.migrate(mockFlywayConfig)

      result.success shouldBe true
      // We check the migrations count here.
      // So every new forward migration expects an increased value here.
      result.migrationsExecuted shouldBe 2
    }
  }
}
