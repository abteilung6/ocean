package org.abteilung6.ocean
package services

import repositories.dto.Account

import org.abteilung6.ocean.repositories.dto.project.MemberResponse

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
      s"To activate you account, please click here $verificationURL"
    )
    send(mail)
  }

  def sendMemberVerification(member: MemberResponse, verificationURL: String): Mail = {
    val mail = Mail(
      "noreply@localhost",
      member.accountEmail,
      s"Invited to project ${member.projectName}",
      s"To confirm your project invitation, please click here $verificationURL"
    )
    send(mail)
  }
}

object EmailService {
  case class Mail(from: String, to: String, subject: String, content: String)
}
