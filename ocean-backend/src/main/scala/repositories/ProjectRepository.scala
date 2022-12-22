package org.abteilung6.ocean
package repositories

import repositories.dto.project.Project
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape
import java.time.Instant
import scala.concurrent.Future

class ProjectTable(tag: Tag) extends Table[Project](tag, "projects") {
  def projectId: Rep[Long] = column[Long]("project_id", O.PrimaryKey, O.AutoInc)

  def name: Rep[String] = column[String]("name")

  def description: Rep[String] = column[String]("description")

  def createdAt: Rep[Instant] = column[Instant]("created_at")

  def ownerId: Rep[Long] = column[Long]("owner_id")

  def owner = foreignKey("fk_owner", ownerId, TableQuery[AccountTable])(
    _.accountId,
    // A project should not exist without an owner.
    // Therefore the project must be deleted before the related account.
    onDelete = ForeignKeyAction.Restrict
  )

  def * : ProvenShape[Project] =
    (
      projectId,
      name,
      description,
      createdAt,
      ownerId
    ) <> ((Project.apply _).tupled, Project.unapply)

}

class ProjectRepository(patchDatabase: Option[Database] = None) {

  private val db = patchDatabase match {
    case Some(value) => value
    case None        => Database.forConfig("postgres-internal")
  }

  val projects = TableQuery[ProjectTable]
  val members = TableQuery[MemberTable]

  def getProjectById(projectId: Long): Future[Option[Project]] = db.run(
    projects.filter(_.projectId === projectId).result.headOption
  )

  def getProjectsByAccountId(accountId: Long): Future[Seq[Project]] = {
    val query = members
      .filter(_.accountId === accountId) // discussion: allow every member state
      .join(projects)
      .on(_.projectId === _.projectId)
      .map { case (_, project) => project }
    db.run(query.result)
  }

  def addProject(project: Project): Future[Project] = db.run(
    projects.returning(projects) += project
  )

  def deleteProject(projectId: Long): Future[Int] = db.run(
    projects.filter(_.projectId === projectId).delete
  )
}
