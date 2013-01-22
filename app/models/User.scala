package models

import anorm._
import play.api.db._
import play.api.Play.current
import anorm.Id
import anorm.SqlParser._

case class User(
  id: Pk[Long],
  fullname: String,
  username: String,
  password: String,
  properties: Set[Property] = Set(),
  objId: Long = 0
) {
  def save: User = {
    DB.withConnection { implicit connection =>
      if (this.id.isDefined) {
        // Save the user
        SQL(
          """
            update user
            set fullname = {fullname}, username = {username}, password = {password}
            where id = {id}
          """
        ).on(
          'id -> this.id,
          'fullname -> this.fullname,
          'username -> this.username,
          'password -> this.password
        ).executeUpdate()

        // Save the properties
        val newProperties = this.properties.map(p => Property(p.id, p.attribute, p.value, this.objId).save)

        // Return the user
        User(this.id, this.fullname, this.username, this.password, newProperties, this.objId)
      } else {
        // Save the user
        val id: Option[Long] = SQL(
          """
            insert into user (fullname, username, password)
            values ({fullname}, {username}, {password})
          """
        ).on(
          'fullname -> this.fullname,
          'username -> this.username,
          'password -> this.password
        ).executeInsert()

        // Save the ViktyoObject
        val obj = ViktyoObject(NotAssigned, 'user, id.get).create

        // Save the properties
        val newProperties = this.properties.map(p => Property(p.id, p.attribute, p.value, obj.id.get).save)

        User(Id(id.get), this.fullname, this.username, this.password, newProperties, obj.id.get)
      }
    }
  }

  def getProperty(attribute: String): Option[String] = {
    val property = this.properties.find(p => p.attribute == attribute)
    if (property.isDefined)
      Some(property.get.value)
    else
      None
  }

  def getPropertyMap: Map[String, String] = {
    this.properties.map(p => (p.attribute, p.value)).toMap
  }

  def setProperty(attribute: String, value: String): User = {
    User(this.id, this.fullname, this.username, this.password,
      this.properties.filterNot(p => p.attribute == attribute) + Property(NotAssigned, attribute, value, 0), this.objId)
  }

  def removeProperty(attribute: String): User = {
    User(this.id, this.fullname, this.username, this.password, this.properties.filterNot(p => p.attribute == attribute), this.objId)
  }
}

object User {

  val simple = {
    get[Pk[Long]]("user.id") ~
    get[String]("user.fullname") ~
    get[String]("user.username") ~
    get[String]("user.password") ~
    get[Long]("object.id") map {
      case id~fullname~username~password~objId => User(id, fullname, username, password, Property.listByObjId(objId), objId)
    }
  }

  def findById(id: Long): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          SELECT object.id, user. *
          FROM object
          JOIN user ON ( user.id = object.objId )
          WHERE object.objType = {userType}
          AND user.id = {id}
        """
      ).on(
        'userType -> ViktyoObject.typeMap('user),
        'id -> id
      ).as(User.simple.singleOpt)
    }
  }
}
