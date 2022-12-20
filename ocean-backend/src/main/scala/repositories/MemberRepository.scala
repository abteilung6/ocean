package org.abteilung6.ocean
package repositories

import repositories.dto.project.{ Member, MemberResponse, MemberState, RoleType }
import repositories.dto.AuthenticatorType
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape
import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MemberTable(tag: Tag) extends Table[Member](tag, "members") {

  implicit val roleTypeColumnType: BaseColumnType[RoleType] =
    MappedColumnType.base[RoleType, String](
      e => e.entryName,
      s => RoleType.withName(s)
    )

  implicit val memberStateColumnType: BaseColumnType[MemberState] =
    MappedColumnType.base[MemberState, String](
      e => e.entryName,
      s => MemberState.withName(s)
    )

  def memberId: Rep[Long] = column[Long]("member_id", O.PrimaryKey, O.AutoInc)

  def roleType: Rep[RoleType] = column[RoleType]("role_type")

  def state: Rep[MemberState] = column[MemberState]("state")

  def createdAt: Rep[Instant] = column[Instant]("created_at")

  def projectId: Rep[Long] = column[Long]("project_id")

  def accountId: Rep[Long] = column[Long]("account_id")

  def project = foreignKey("fk_project", projectId, TableQuery[ProjectTable])(
    _.projectId,
    // Delete the member before the project.
    onDelete = ForeignKeyAction.Restrict
  )

  def account = foreignKey("fk_account", accountId, TableQuery[AccountTable])(
    _.accountId,
    // Delete the member before the account.
    onDelete = ForeignKeyAction.Restrict
  )

  def * : ProvenShape[Member] =
    (
      memberId,
      roleType,
      state,
      projectId,
      accountId,
      createdAt
    ) <> ((Member.apply _).tupled, Member.unapply)
}

class MemberRepository(patchDatabase: Option[Database] = None) {

  private val db = patchDatabase match {
    case Some(value) => value
    case None        => Database.forConfig("postgres-internal")
  }

  // Duplicated code. Lets extract the implicit in the future,
  // but keep slick dependencies away from object.
  implicit val authenticatorTypeColumnType: BaseColumnType[AuthenticatorType] =
    MappedColumnType.base[AuthenticatorType, String](
      e => e.entryName,
      s => AuthenticatorType.withName(s)
    )

  implicit val roleTypeColumnType: BaseColumnType[RoleType] =
    MappedColumnType.base[RoleType, String](
      e => e.entryName,
      s => RoleType.withName(s)
    )

  implicit val memberStateColumnType: BaseColumnType[MemberState] =
    MappedColumnType.base[MemberState, String](
      e => e.entryName,
      s => MemberState.withName(s)
    )

  val members = TableQuery[MemberTable]
  val accounts = TableQuery[AccountTable]
  val projects = TableQuery[ProjectTable]

  def getMemberById(memberId: Long): Future[Option[MemberResponse]] = {
    val query = members
      .filter(_.memberId === memberId)
      .join(accounts)
      .on(_.accountId === _.accountId)
      .join(projects)
      .on(_._1.projectId === _.projectId)
      .map { case ((member, account), project) =>
        (
          member.memberId,
          member.roleType,
          member.state,
          member.createdAt,
          account.accountId,
          account.username,
          account.authenticatorType,
          account.email,
          project.projectId,
          project.name
        )
      }
    db.run(
      query.result.headOption
    ).map { future =>
      future.map(parameters => (MemberResponse.apply _).tupled(parameters))
    }
  }

  def getMemberByAccountId(accountId: Long, projectId: Long): Future[Option[MemberResponse]] = {
    val query = members
      .filter(_.accountId === accountId)
      .filter(_.projectId === projectId)
      .join(accounts)
      .on(_.accountId === _.accountId)
      .join(projects)
      .on(_._1.projectId === _.projectId)
      .map { case ((member, account), project) =>
        (
          member.memberId,
          member.roleType,
          member.state,
          member.createdAt,
          account.accountId,
          account.username,
          account.authenticatorType,
          account.email,
          project.projectId,
          project.name
        )
      }
    db.run(
      query.result.headOption
    ).map { future =>
      future.map(parameters => (MemberResponse.apply _).tupled(parameters))
    }
  }

  def getMembersByProjectId(projectId: Long): Future[Seq[MemberResponse]] = {
    val query = members
      .filter(_.projectId === projectId)
      .join(accounts)
      .on(_.accountId === _.accountId)
      .join(projects)
      .on(_._1.projectId === _.projectId)
      .map { case ((member, account), project) =>
        (
          member.memberId,
          member.roleType,
          member.state,
          member.createdAt,
          account.accountId,
          account.username,
          account.authenticatorType,
          account.email,
          project.projectId,
          project.name
        )
      }
    db.run(query.result).map { sequence =>
      sequence.map(parameters => (MemberResponse.apply _).tupled(parameters))
    }
  }

  def addMember(member: Member): Future[MemberResponse] =
    for {
      addedMember <- db.run(
        members.returning(members) += member
      )
      receivedMember <- getMemberById(addedMember.memberId)
    } yield receivedMember.get

  def acceptMemberById(memberId: Long): Future[Option[MemberResponse]] =
    for {
      _ <- db.run(members.filter(_.memberId === memberId).map(_.state).update(MemberState.Active))
      updatedMember <- getMemberById(memberId)
    } yield updatedMember

  def deleteMemberById(memberId: Long): Future[Int] = db.run(
    members.filter(_.memberId === memberId).delete
  )
}
