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
          |  "id" : 0,
          |  "username" : "username1",
          |  "email" : "username1@localhost",
          |  "firstname" : "firstname1",
          |  "lastname" : "lastname2",
          |  "employeeType" : "student",
          |  "createdAt" : "2022-11-09T19:14:31.903Z",
          |  "authenticatorType" : "credentials"
          |}""".stripMargin
    val dummyAccount = getMockAccount(
      createdAt = Instant.parse("2022-11-09T19:14:31.903Z"),
      authenticatorType = AuthenticatorType.Credentials
    )

    "encode" in {
      import io.circe.syntax._
      dummyAccount.asJson.spaces2 shouldBe accountString
    }

    "decode" in {
      import io.circe.parser.decode
      val actual = decode[Account](accountString)
      actual.value shouldBe dummyAccount
    }
  }
}
