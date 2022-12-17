package org.abteilung6.ocean
package controllers

import controllers.endpoints.EndpointController
import repositories.dto.Account
import repositories.dto.project.{ CreateMemberRequest, MemberResponse, MemberVerificationTokenContent }
import repositories.dto.response.ResponseError
import services.{ EmailService, JwtService, MemberService }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import org.abteilung6.ocean.services.EmailService.Mail
import org.abteilung6.ocean.utils.RuntimeConfig
import sttp.tapir.AnyEndpoint
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter
import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MemberController(
  endpointController: EndpointController,
  memberService: MemberService,
  emailService: EmailService,
  jwtService: JwtService,
  runtimeConfig: RuntimeConfig
) extends BaseController
    with FailFastCirceSupport {

  import repositories.dto.project.CreateMemberRequest.Implicits._
  import repositories.dto.project.MemberResponse.Implicits._

  override val basePath: String = "member"

  override val tag: String = "Member"

  override def route: Route = AkkaHttpServerInterpreter().toRoute(List(createMemberEndpoint))

  override def endpoints: List[AnyEndpoint] = List(createMemberEndpoint.endpoint)

  val createMemberEndpoint
    : ServerEndpoint.Full[String, Account, CreateMemberRequest, ResponseError, MemberResponse, Any, Future] =
    endpointController.secureEndpointWithUser.post
      .tag(tag)
      .description("Create a member for a project")
      .in(basePath)
      .in(jsonBody[CreateMemberRequest])
      .out(jsonBody[MemberResponse])
      .serverLogic { (account: Account) => createMemberRequest =>
        val futureMember = for {
          member <- memberService.createMember(createMemberRequest, account)
        } yield member
        futureMember
          .map { member =>
            sendProjectInvitationMail(member)
            Right(member)
          }
          .recover {
            case e: MemberService.Exceptions.InsufficientPermissionException =>
              Left(ResponseError(StatusCodes.Forbidden.intValue, e.message))
            case e: MemberService.Exceptions.MemberExistsException =>
              Left(ResponseError(StatusCodes.BadRequest.intValue, e.message))
            case e: MemberService.Exceptions.ProjectDoesNotExistException =>
              Left(ResponseError(StatusCodes.BadRequest.intValue, e.message))
            case _ => Left(ResponseError(StatusCodes.InternalServerError.intValue, "Something went wrong"))
          }
      }

  def sendProjectInvitationMail(memberResponse: MemberResponse): Mail = {
    val memberVerificationTokenContent = MemberVerificationTokenContent("member", memberResponse.memberId)
    val token =
      jwtService.encodeMemberVerificationTokenContent(memberVerificationTokenContent, Instant.now.getEpochSecond)
    val serviceBindingConfig = runtimeConfig.serverBindingConfig
    val verificationUrl =
      this.toAbsoluteURI(
        serviceBindingConfig.interface,
        serviceBindingConfig.port,
        this.toRelativeURI("accept"),
        Map("token" -> token)
      )
    emailService.sendMemberVerification(memberResponse, verificationUrl)
  }
}
