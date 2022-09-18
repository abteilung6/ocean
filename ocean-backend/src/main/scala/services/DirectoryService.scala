package org.abteilung6.ocean
package services

import org.apache.directory.api.ldap.model.cursor.{ CursorException, SearchCursor }
import org.apache.directory.api.ldap.model.entry.Entry
import org.apache.directory.api.ldap.model.exception.{
  LdapAuthenticationException,
  LdapException,
  LdapTimeLimitExceededException
}
import org.apache.directory.api.ldap.model.message.{ SearchRequest, SearchRequestImpl, SearchScope }
import org.apache.directory.api.ldap.model.name.Dn
import org.apache.directory.ldap.client.api.{ LdapConnectionConfig, LdapNetworkConnection }
import org.apache.directory.ldap.client.api.exception.InvalidConnectionException
import scala.util.{ Failure, Success, Try }
import services.exceptions.ServiceException
import utils.RuntimeConfig

class DirectoryService(runtimeConfig: RuntimeConfig) {

  import DirectoryService.Exceptions._
  import DirectoryService._

  def authenticate(username: String, password: String): Try[UserEntry] =
    bindRequest(username, password) match {
      case Failure(exception) => Failure(exception)
      case Success(networkConnection) =>
        searchWithSearchRequest(networkConnection, username) match {
          case Failure(exception) => Failure(exception)
          case Success(entry)     => Success(entry)
        }
    }

  def bindRequest(username: String, password: String): Try[LdapNetworkConnection] = {
    val connectionConfig = getConnectionConfig(username, password)
    val networkConnection = new LdapNetworkConnection(connectionConfig)
    try {
      networkConnection.bind()
      Success(networkConnection)
    } catch {
      case _: LdapAuthenticationException    => Failure(AccessDenied("Invalid credentials"))
      case _: LdapTimeLimitExceededException => Failure(InternalError("Time limit exceeded"))
      case _: InvalidConnectionException     => Failure(InternalError("Connection refused"))
      case exception: LdapException          => Failure(InternalError(exception.getMessage))
    }
  }

  private def getConnectionConfig(username: String, password: String): LdapConnectionConfig = {
    val directoryConfig = runtimeConfig.directoryConfig
    val connectionConfig = new LdapConnectionConfig()
    connectionConfig.setLdapHost(directoryConfig.host)
    connectionConfig.setLdapPort(directoryConfig.port)
    connectionConfig.setUseSsl(directoryConfig.startTls)
    connectionConfig.setUseSsl(directoryConfig.useSsl)
    connectionConfig.setName(
      directoryConfig.name
        .replace("%USER%", username)
        .replace("%USER_ROOT%", directoryConfig.userRoot)
    )
    connectionConfig.setCredentials(password)
    connectionConfig
  }

  def searchWithSearchRequest(networkConnection: LdapNetworkConnection, username: String): Try[UserEntry] = {
    val searchRequest: SearchRequest = new SearchRequestImpl()
    searchRequest.setScope(SearchScope.ONELEVEL)
    searchRequest.addAttributes("*")
    searchRequest.setBase(new Dn(runtimeConfig.directoryConfig.userRoot))
    searchRequest.setFilter(s"(cn=$username)")

    try {
      val searchCursor: SearchCursor = networkConnection.search(searchRequest)
      searchCursor.next()
      searchCursor.get()
      val userEntry = unmarshalUserEntryFrom(searchCursor.getEntry)
      Success(userEntry)
    } catch {
      case _: CursorException => Failure(AccessDenied())
      case _: LdapException   => Failure(AccessDenied())
      case _: Exception       => Failure(InternalError())
    }
  }

  private def unmarshalUserEntryFrom(entry: Entry): UserEntry =
    UserEntry(
      getDefinedValue(entry, "uid"),
      getDefinedValue(entry, "name"),
      getDefinedValue(entry, "givenName"),
      getDefinedValue(entry, "sn"),
      getDefinedValue(entry, "mail"),
      getDefinedValue(entry, "employeetype"),
      getDefinedValue(entry, "accountexpires")
    )

  private def getDefinedValue(entry: Entry, key: String, fallbackValue: String = ""): String =
    try entry.get(key).getString
    catch {
      case _: Throwable => fallbackValue
    }
}

object DirectoryService {
  object Exceptions {
    sealed abstract class DirectoryServiceException(message: String) extends ServiceException(message)

    final case class AccessDenied(message: String = "Access denied") extends DirectoryServiceException(message)

    final case class InternalError(message: String = "Internal error") extends DirectoryServiceException(message)
  }

  case class UserEntry(
    uid: String,
    name: String,
    givenName: String,
    sn: String,
    mail: String,
    employeeType: String,
    accountExpires: String
  )
}
