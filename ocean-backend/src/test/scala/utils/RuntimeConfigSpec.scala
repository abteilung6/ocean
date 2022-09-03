package org.abteilung6.ocean
package utils

import com.typesafe.config.{Config, ConfigException, ConfigFactory}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class RuntimeConfigSpec extends AnyWordSpec with Matchers {

  "A ServerBindingConfig" should {
    "load interface and port" in {
      val applicationConfig: String =
        s"""
           |server-binding {
           |    interface = "localhost"
           |    port = 8080
           |}
           |""".stripMargin
      val config: Config = ConfigFactory.parseString(applicationConfig)
      val runtimeConfig = new RuntimeConfig(config)

      runtimeConfig.serverBindingConfig.interface shouldEqual "localhost"
      runtimeConfig.serverBindingConfig.port shouldEqual 8080
    }

    "throw a config exception if config is missing a key" in {
      val applicationConfig: String =
        s"""
           |server-binding {
           |}
           |""".stripMargin
      val config: Config = ConfigFactory.parseString(applicationConfig)
      val runtimeConfig = new RuntimeConfig(config)

      assertThrows[ConfigException] {
        runtimeConfig.serverBindingConfig
      }
    }
  }
}
