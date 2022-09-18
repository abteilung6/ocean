package org.abteilung6.ocean
package services

import org.apache.directory.ldap.client.api.LdapNetworkConnection
import org.mockito.ArgumentMatchers.{ any, anyString }
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.TryValues._
import scala.util.Success
import services.DirectoryService.UserEntry

class DirectoryServiceSpec extends AnyWordSpec with Matchers with MockitoSugar {

  "authenticate" should {
    "return a UserEntry if authentication is successful" in {
      val directoryService = mock[DirectoryService]
      val userEntry = UserEntry("uid", "name", "givenName", "sn", "mail", "employeeType", "accountExpires")

      when(directoryService.bindRequest(anyString(), anyString()))
        .thenReturn(Success(new LdapNetworkConnection()))
      when(directoryService.searchWithSearchRequest(any[LdapNetworkConnection], anyString()))
        .thenReturn(Success(userEntry))
      when(directoryService.authenticate(anyString(), anyString()))
        .thenCallRealMethod()

      val actual = directoryService.authenticate("username", "password")
      actual.success.value shouldBe userEntry
    }
  }
}
