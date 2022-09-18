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
}

object RuntimeConfig {
  def load(config: Config = ConfigFactory.load()): RuntimeConfig =
    new RuntimeConfig(config)
}
