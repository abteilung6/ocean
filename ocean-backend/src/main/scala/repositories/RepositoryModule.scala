package org.abteilung6.ocean
package repositories

import utils.RuntimeConfig
import slick.jdbc.JdbcBackend.Database

trait RepositoryModule {

  import com.softwaremill.macwire._

  val runtimeConfig: RuntimeConfig = RuntimeConfig.load()
  lazy val patchDatabase: Option[Database] = None

  lazy val accountRepository: AccountRepository = wire[AccountRepository]
}
