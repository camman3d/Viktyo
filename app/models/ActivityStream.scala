package models

import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import anorm.~
import anorm.Id

/**
 * Based off the specification found here:
 * http://activitystrea.ms/
 * @param id The id of the object
 * @param published Timestamp
 * @param actor Id of the user that did this
 * @param verb What kind of thing the user did
 * @param obj ID of the ViktyoObject that the user did
 * @param target ID of the ViktyoObject where the user did his thing
 */
case class ActivityStream(
  id: Pk[Long],
  published: Long, // Timestamp
  actor: User,
  verb: String,
  obj: Long,
  target: Long // User if messaging or status updates, Posting or image if comment
)  {
  def save: ActivityStream = {
    DB.withConnection { implicit connection =>
      if (this.id.isDefined) {
        // Save the activity stream
        SQL(
          """
            update activity_stream
            set published = {published}, actor = {actor}, verb = {verb}, obj = {obj}, target = {target}
            where id = {id}
          """
        ).on(
          'id -> this.id,
          'published -> this.published,
          'actor -> this.actor.id.get,
          'verb -> this.verb,
          'obj -> this.obj,
          'target -> this.target
        ).executeUpdate()

        // Return the activity stream
        this
      } else {
        // Save the activity stream
        val id: Option[Long] = SQL(
          """
            insert into activity_stream (published, actor, verb, obj, target)
            values ({published}, {actor}, {verb}, {obj}, {target})
          """
        ).on(
          'published -> this.published,
          'actor -> this.actor.id.get,
          'verb -> this.verb,
          'obj -> this.obj,
          'target -> this.target
        ).executeInsert()

        // Return the activity stream
        ActivityStream(Id(id.get), this.published, this.actor, this.verb, this.obj, this.target)
      }
    }
  }

  def delete() {
    DB.withConnection { implicit connection =>
      SQL("delete from activity_stream where id = {id}").on('id -> this.id.get).executeUpdate()
    }
  }
}

object ActivityStream {
  val simple = {
    get[Pk[Long]]("activity_stream.id") ~
      get[Long]("activity_stream.published") ~
      get[Long]("activity_stream.actor") ~
      get[String]("activity_stream.verb") ~
      get[Long]("activity_stream.obj") ~
      get[Long]("activity_stream.target") map {
      case id~published~actor~verb~obj~target => ActivityStream(id, published, User.findById(actor).get, verb, obj, target)
    }
  }

  def findById(id: Long): Option[ActivityStream] = {
    DB.withConnection { implicit connection =>
      SQL("select * from activity_stream where id = {id}").on('id -> id).as(ActivityStream.simple.singleOpt)
    }
  }

  def listByActor(id: Long, page: (Int, Int) = (20, 0)): List[ActivityStream] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          select * from activity_stream where actor = {id}
          order by published desc
          limit {pageSize} offset {offset}
        """
      ).on(
        'id -> id,
        'pageSize -> page._1,
        'offset -> page._2
      ).as(ActivityStream.simple *)
    }
  }

  def listByTarget(id: Long, page: Int = 0, pageSize: Int = 10): List[ActivityStream] = {
    val offset = page * pageSize
    DB.withConnection { implicit connection =>
      SQL(
        """
          select * from activity_stream where target = {id}
          order by published desc
          limit {pageSize} offset {offset}
        """
      ).on(
        'id -> id,
        'pageSize -> pageSize,
        'offset -> offset
      ).as(ActivityStream.simple *)
    }
  }

}
