package org.abteilung6.ocean
package repositories

import utils.UtilsModule
import slick.jdbc.JdbcBackend.Database

trait RepositoryModule extends UtilsModule {

  import com.softwaremill.macwire._

  lazy val patchDatabase: Option[Database] = None

  lazy val accountRepository: AccountRepository = wire[AccountRepository]
  lazy val projectRepository: ProjectRepository = wire[ProjectRepository]
  lazy val memberRepository: MemberRepository = wire[MemberRepository]
}
