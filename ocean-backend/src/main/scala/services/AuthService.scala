package org.abteilung6.ocean
package services

import repositories.AccountRepository
import scala.concurrent.ExecutionContext.Implicits.global

class AuthService(directoryService: DirectoryService, accountRepository: AccountRepository) {

  def authenticate(): Boolean = {
    accountRepository.getUserByUsername("user01").onComplete(foo => print(foo))
    // print(directoryService.authenticate("user01", "password1"))
    true
  }
}
