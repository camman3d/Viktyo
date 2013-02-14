package models

import anorm._
import play.api.db.DB
import play.api.Play.current
import anorm.SqlParser._
import anorm.~
import org.apache.commons.lang3.StringEscapeUtils
import play.api.libs.json.Json
import java.util.Date

/**
 * Possible notifications
 * 'following and (follower -> userId) means "So and so is now following you"
 * 'message and (message -> message) means a user to user message
 *
 * @param id The id
 * @param user The user this notification is directed to
 * @param from The user this is from (-1 for system notification)
 * @param notificationType Can be 'following or 'message
 * @param data Data to be used in displaying the message
 * @param created Time of notification
 * @param read Has the recieved read this yet?
 */
case class ViktyoNotification(
                               id: Pk[Long],
                               user: Long,
                               from: Long,
                               notificationType: Symbol,
                               data: Map[String, String],
                               created: Long,
                               read: Boolean
                               ) {


  def save: ViktyoNotification = {
    DB.withConnection {
      implicit connection =>
        if (this.id.isDefined) {
          // Save the notification
          SQL(
            """
            update notification
            set user = {user}, `from` = {from}, notificationType = {notificationType}, data = {data}, created = {created},
            `read` = {read} where id = {id}
            """
          ).on(
            'id -> this.id,
            'user -> this.user,
            'from -> this.from,
            'notificationType -> this.notificationType.name,
            'data -> Json.toJson(data).toString(),
            'created -> this.created,
            'read -> this.read
          ).executeUpdate()

          // Return the notification
          this
        } else {
          // Save the notification
          val id: Option[Long] = SQL(
            """
            insert into notification (user, `from`, notificationType, data, created, `read`)
            values ({user}, {from}, {notificationType}, {data}, {created}, {read})
            """
          ).on(
            'user -> this.user,
            'from -> this.from,
            'notificationType -> this.notificationType.name,
            'data -> Json.toJson(data).toString(),
            'created -> this.created,
            'read -> this.read
          ).executeInsert()

          // Return the notification
          ViktyoNotification(
            Id(id.get), this.user, this.from, this.notificationType, this.data, this.created, this.read
          )
        }
    }
  }

  def markRead: ViktyoNotification =
    ViktyoNotification(this.id, this.user, this.from, this.notificationType, this.data, this.created, true)
}

object ViktyoNotification {
  val simple = {
    get[Pk[Long]]("notification.id") ~
      get[Long]("notification.user") ~
      get[Long]("notification.from") ~
      get[String]("notification.notificationType") ~
      get[String]("notification.data") ~
      get[Long]("notification.created") ~
      get[Boolean]("notification.read") map {
      case id ~ user ~ from ~ notificationType ~ data ~ created ~ read =>
        ViktyoNotification(
          id, user, from, Symbol(notificationType), Json.parse(data).as[Map[String, String]], created, read
        )
    }
  }

  def findById(id: Long): Option[ViktyoNotification] = {
    DB.withConnection {
      implicit connection =>
        SQL("SELECT * from notification where id = {id}").on('id -> id).as(ViktyoNotification.simple.singleOpt)
    }
  }

  def listByUser(user: Long): List[ViktyoNotification] = {
    DB.withConnection {
      implicit connection =>
        SQL("SELECT * from notification where user = {user} order by created desc").on('user -> user)
          .as(ViktyoNotification.simple *)
    }
  }

  def createFollowing(follower: User, followee: User): ViktyoNotification =
    ViktyoNotification(NotAssigned, followee.id.get, -1, 'following, Map("follower" -> follower.id.get.toString),
      new Date().getTime, false)

  def createMessage(sender: User, receiver: User, message: String): ViktyoNotification =
    ViktyoNotification(NotAssigned, receiver.id.get, sender.id.get, 'message, Map("message" -> message),
      new Date().getTime, false)
}
