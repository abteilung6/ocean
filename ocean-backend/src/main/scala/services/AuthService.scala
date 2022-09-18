package org.abteilung6.ocean
package services

class AuthService(directoryService: DirectoryService) {

  def authenticate(): Boolean = {
    print(directoryService.authenticate("user01", "password1"))
    true
  }
}
