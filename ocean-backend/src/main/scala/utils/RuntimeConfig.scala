package org.abteilung6.ocean
package utils

import com.typesafe.config.{Config, ConfigFactory}

sealed trait ConfigCatalog

final case class ServerBindingConfig(interface: String, port: Int) extends ConfigCatalog

class RuntimeConfig(val config: Config) {

  lazy val serverBindingConfig: ServerBindingConfig = {
    val _config = config.getConfig("server-binding")
    ServerBindingConfig(
      interface = _config.getString("interface"),
      port = _config.getInt("port"),
    )
  }
}

object RuntimeConfig {
  def load(config: Config = ConfigFactory.load()): RuntimeConfig =
    new RuntimeConfig(config)
}
