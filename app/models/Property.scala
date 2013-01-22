package models

import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._
import anorm.~
import anorm.Id

case class Property(id: Pk[Long], attribute: String, value: String, objId: Long) {
  def save: Property = {
    DB.withConnection { implicit connection =>
      if (this.id.isDefined) {
        SQL(
          """
            update property
            set attribute = {attribute}, value = {value}, objId = {objId}
            where id = {id}
          """
        ).on(
          'id -> id,
          'attribute -> this.attribute,
          'value -> this.value,
          'objId -> this.objId
        ).executeUpdate()
        this
      } else {
        val id: Option[Long] = SQL(
          """
            insert into property (attribute, value, objId)
            values ({attribute}, {value}, {objId})
          """
        ).on(
          'attribute -> this.attribute,
          'value -> this.value,
          'objId -> this.objId
        ).executeInsert()
        Property(Id(id.get), this.attribute, this.value, this.objId)
      }
    }
  }
}

object Property {

  val simple = {
    get[Pk[Long]]("property.id") ~
    get[String]("property.attribute") ~
    get[String]("property.value") ~
    get[Long]("property.objId") map {
      case id~attribute~value~objId => Property(id, attribute, value, objId)
    }
  }

  def listByObjId(id: Long): Set[Property] = {
    DB.withConnection { implicit connection =>
      SQL("select * from property where objId = {objId}").on('objId -> id).as(Property.simple *).toSet
    }
  }
}