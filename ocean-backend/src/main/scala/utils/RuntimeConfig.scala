package org.abteilung6.ocean
package utils

import com.typesafe.config.{ Config, ConfigFactory }

sealed trait ConfigCatalog

final case class ServerBindingConfig(interface: String, port: Int) extends ConfigCatalog

final case class DirectoryConfig(
  host: String,
  port: Int,
  startTls: Boolean,
  useSsl: Boolean,
  name: String,
  userRoot: String
) extends ConfigCatalog

final case class FlywayConfig(
  url: String,
  user: String,
  password: String,
  locations: String
)

final case class JwtConfig(
  key: String,
  accessExpirationTimeInSeconds: Long,
  refreshExpirationTimeInSeconds: Long
)

final case class OpenAPIConfig(
  title: String,
  version: String
)

class RuntimeConfig(val config: Config) {

  lazy val serverBindingConfig: ServerBindingConfig = {
    val _config = config.getConfig("server-binding")
    ServerBindingConfig(
      interface = _config.getString("interface"),
      port = _config.getInt("port")
    )
  }

  lazy val directoryConfig: DirectoryConfig = {
    val _config = config.getConfig("directory")
    DirectoryConfig(
      _config.getString("host"),
      _config.getInt("port"),
      _config.getBoolean("startTls"),
      _config.getBoolean("useSsl"),
      _config.getString("name"),
      _config.getString("userRoot")
    )
  }

  lazy val flywayConfig: FlywayConfig = {
    val _config = config.getConfig("flyway")
    FlywayConfig(
      _config.getString("url"),
      _config.getString("user"),
      _config.getString("password"),
      _config.getString("locations")
    )
  }

  lazy val jwtConfig: JwtConfig = {
    val _config = config.getConfig("jwt")
    JwtConfig(
      _config.getString("key"),
      _config.getLong("accessExpirationTimeInSeconds"),
      _config.getLong("refreshExpirationTimeInSeconds")
    )
  }

  lazy val openAPIConfig: OpenAPIConfig = {
    val _config = config.getConfig("openapi")
    OpenAPIConfig(
      _config.getString("title"),
      _config.getString("version")
    )
  }
}

object RuntimeConfig {
  def load(config: Config = ConfigFactory.load()): RuntimeConfig =
    new RuntimeConfig(config)
}
