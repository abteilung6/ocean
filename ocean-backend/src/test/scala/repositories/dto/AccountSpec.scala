package org.abteilung6.ocean
package repositories.dto

import repositories.utils.TestMockUtils.getMockAccount
import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import java.time.Instant

class AccountSpec extends AnyWordSpec with Matchers with MockitoSugar with EitherValues {

  "Account" should {
    import repositories.dto.Account.Implicits._

    val accountString =
      s"""|{
          |  "accountId" : 0,
          |  "email" : "username1@localhost.com",
          |  "firstname" : "firstname1",
          |  "lastname" : "lastname2",
          |  "company" : "student",
          |  "createdAt" : "2022-11-09T19:14:31.903Z",
          |  "authenticatorType" : "credentials",
          |  "verified" : false,
          |  "passwordHash" : "hash"
          |}""".stripMargin
    val dummyAccount = getMockAccount(
      createdAt = Instant.parse("2022-11-09T19:14:31.903Z"),
      authenticatorType = AuthenticatorType.Credentials,
      passwordHash = Some("hash")
    )

    "encode" in {
      import io.circe.syntax._
      dummyAccount.asJson.spaces2.stripMargin
        .filter(_ >= ' ') shouldBe
        accountString
          .filter(_ >= ' ')
    }

    "decode" in {
      import io.circe.parser.decode
      val actual = decode[Account](accountString)
      actual.value shouldBe dummyAccount
    }

    "decode null values" in {
      import io.circe.parser.decode
      import io.circe.syntax._

      val nullableAccount = getMockAccount(passwordHash = None)
      val nullableAccountString = nullableAccount.asJson.spaces2

      decode[Account](nullableAccountString).value shouldBe nullableAccount
    }
  }
}
