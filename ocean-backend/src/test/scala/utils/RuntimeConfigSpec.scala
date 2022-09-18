package org.abteilung6.ocean
package utils

import com.typesafe.config.{ Config, ConfigException, ConfigFactory }
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class RuntimeConfigSpec extends AnyWordSpec with Matchers {

  "A ServerBindingConfig" should {
    "load interface and port" in {
      val configString: String =
        s"""
           |server-binding {
           |    interface = "localhost"
           |    port = 8080
           |}
           |""".stripMargin
      val config: Config = ConfigFactory.parseString(configString)
      val serverBindingConfig = RuntimeConfig.load(config).serverBindingConfig

      serverBindingConfig.interface shouldEqual "localhost"
      serverBindingConfig.port shouldEqual 8080
    }

    "throw a config exception if entry is missing" in {
      val configString: String =
        s"""
           |server-binding {
           |}
           |""".stripMargin
      val config: Config = ConfigFactory.parseString(configString)

      assertThrows[ConfigException] {
        RuntimeConfig.load(config).serverBindingConfig
      }
    }
  }

  "A DirectoryConfig" should {
    "load its config" in {
      val configString: String =
        s"""
           |directory {
           |    host = "127.0.0.1"
           |    port = 1389
           |    startTls = false
           |    useSsl = false
           |    name = "cn=%USER%,%USER_ROOT%"
           |    userRoot = "ou=users,dc=example,dc=org"
           |}
           |""".stripMargin
      val config: Config = ConfigFactory.parseString(configString)
      val directoryConfig = RuntimeConfig.load(config).directoryConfig

      directoryConfig.host shouldEqual "127.0.0.1"
      directoryConfig.port shouldEqual 1389
      directoryConfig.startTls shouldEqual false
      directoryConfig.useSsl shouldEqual false
      directoryConfig.name shouldEqual "cn=%USER%,%USER_ROOT%"
      directoryConfig.userRoot shouldEqual "ou=users,dc=example,dc=org"
    }

    "throw a config exception if entry is missing" in {
      val configString: String =
        s"""
           |directory {
           |}
           |""".stripMargin
      val config: Config = ConfigFactory.parseString(configString)

      assertThrows[ConfigException] {
        RuntimeConfig.load(config).directoryConfig
      }
    }
  }
}
