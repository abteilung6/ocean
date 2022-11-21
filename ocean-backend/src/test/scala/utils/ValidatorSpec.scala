package org.abteilung6.ocean
package utils

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ValidatorSpec extends AnyWordSpec with Matchers {

  "validateEmail" should {
    val validEmailPrefixes: Seq[String] =
      List(
        "test-d@domain.com",
        "test.def@domain.com",
        "test+1@domain.com",
        "test-1@domain.com",
        "test_def@domain.com",
        "test@domain.com"
      )

    val invalidEmailPrefix: Seq[String] =
      List("", "1", "abc..def@domain.com")

    val validEmailDomains: Seq[String] =
      List(
        "abc.def@domain.cc",
        "abc.def@domain-archive.com",
        "abc.def@domain.org",
        "abc.def@domain.com"
      )

    val invalidEmailDomains: Seq[String] =
      List(
        "abc.def@domain#archive.com",
        "abc.def@domain",
        "abc.def@domain..com"
      )

    validEmailPrefixes.foreach { spec =>
      s"Prefix ${spec} is valid" in {
        Validator.validateEmail(spec) shouldBe true
      }
    }
    invalidEmailPrefix.foreach { spec =>
      s"Prefix ${spec} is invalid" in {
        Validator.validateEmail(spec) shouldBe false
      }
    }
    validEmailDomains.foreach { spec =>
      s"Domain ${spec} is invalid" in {
        Validator.validateEmail(spec) shouldBe true
      }
    }
    invalidEmailDomains.foreach { spec =>
      s"Domain ${spec} is invalid" in {
        Validator.validateEmail(spec) shouldBe false
      }
    }
  }
  "isAlphanumeric" should {
    val validAlphanumeric = List("abc", "123", "a1b2", "0")
    val invalidAlphanumeric = List("!", "#", "?", "_", "-", ".")

    validAlphanumeric.foreach { spec =>
      s"Sequence ${spec} is alphanumeric" in {
        Validator.isAlphanumeric(spec) shouldBe true
      }
    }

    invalidAlphanumeric.foreach { spec =>
      s"Sequence ${spec} is not alphanumeric" in {
        Validator.isAlphanumeric(spec) shouldBe false
      }
    }
  }

  "min" should {
    "validate the minimal length" in {
      Validator.min("", 0) shouldBe true
      Validator.min("a", 0) shouldBe true
      Validator.min("a", 1) shouldBe true
      Validator.min("a?!", 3) shouldBe true

      Validator.min("a", 2) shouldBe false
      Validator.min("aa", 3) shouldBe false
    }
  }

  "max" should {
    "validate the max length" in {
      Validator.max("", 0) shouldBe true
      Validator.max("a", 1) shouldBe true
      Validator.max("a!", 3) shouldBe true

      Validator.max("a", 0) shouldBe false
      Validator.max("aa", 1) shouldBe false
      Validator.max("aa!?", 3) shouldBe false
    }
  }
}
