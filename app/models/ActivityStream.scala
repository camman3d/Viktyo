package models

import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import anorm.~
import anorm.Id
import java.util.Date

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
      if (verb == "comment" || verb == "statusUpdate")
        Text.findById(obj).get.delete()
      if (verb == "imagePost")
        Image.findById(obj).get.delete()
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

  def find(user: User, verb: String, obj: Long, target: Long): Option[ActivityStream] = {
    DB.withConnection { implicit connection =>
      SQL(
        "select * from activity_stream where user = {user}, verb = {verb}, obj = {obj}, target = {target}"
      ).on(
        'user -> user.id.get,
        'verb -> verb,
        'obj -> obj,
        'target -> target
      ).as(ActivityStream.simple.singleOpt)
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

  def listByObject(id: Long, page: (Int, Int) = (20, 0)): List[ActivityStream] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          select * from activity_stream where obj = {id}
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

  def createComment(user: User, comment: String, target: Long): ActivityStream = {
    val text = Text(NotAssigned, comment).save
    ActivityStream(NotAssigned, new Date().getTime, user, "comment", text.objId, target)
  }

  def createImagePost(user: User, image: Image, target: Long): ActivityStream = {
    ActivityStream(NotAssigned, new Date().getTime, user, "imagePost", image.objId, target)
  }

  def createProfilePicture(user: User, image: Image): ActivityStream = {
    ActivityStream(NotAssigned, new Date().getTime, user, "profilePicture", image.objId, user.objId)
  }

  def createFavorite(user: User, posting: Posting): ActivityStream = {
    ActivityStream(NotAssigned, new Date().getTime, user, "favorite", posting.objId, posting.objId)
  }

  def createFollowPosting(user: User, posting: Posting): ActivityStream = {
    ActivityStream(NotAssigned, new Date().getTime, user, "follow", posting.objId, user.objId)
  }

  def createFollowUser(user: User, otherUser: User): ActivityStream = {
    ActivityStream(NotAssigned, new Date().getTime, user, "follow", otherUser.objId, user.objId)
  }

  def createNetworkJoin(user: User, network: Network): ActivityStream = {
    ActivityStream(NotAssigned, new Date().getTime, user, "join", network.objId, network.objId)
  }

  /**
    * For a status update the target is the user and the object is the update as text.
   * @param user The user making the update
   * @param update The update
   * @return
   */
  def createStatusUpdate(user: User, update: String, target: Long): ActivityStream = {
    val text = Text(NotAssigned, update).save
    ActivityStream(NotAssigned, new Date().getTime, user, "statusUpdate", text.objId, target)
  }


}
