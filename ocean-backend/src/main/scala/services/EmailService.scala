package org.abteilung6.ocean
package services

import repositories.dto.Account

class EmailService {

  import EmailService.Mail

  def send(mail: Mail): Mail = {
    println(s"[EmailService] send: $mail")
    mail
  }

  def sendRegistrationVerification(account: Account, verificationURL: String): Mail = {
    val mail = Mail(
      "noreply@localhost",
      account.email,
      "Please confirm your registration",
      s"To activate you account, please click here $verificationURL to confirm you registration."
    )
    send(mail)
  }
}

object EmailService {
  case class Mail(from: String, to: String, subject: String, content: String)
}
