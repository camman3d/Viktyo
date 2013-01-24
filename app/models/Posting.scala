package models

import anorm._
import play.api.db.DB
import play.api.Play.current
import anorm.SqlParser._
import anorm.~

case class Posting(
                    id: Pk[Long],
                    name: String,
                    posted: Long, // Timestamp
                    poster: User, // User
                    location: Location, // Location
                    properties: Set[Property] = Set(),
                    objId: Long = 0
                    ) {
  def save: Posting = {
    DB.withConnection {
      implicit connection =>
        if (this.id.isDefined) {
          // Save the posting
          SQL(
            """
            update posting
            set name = {name}, posted = {posted}, poster = {poster}, location = {location}
            where id = {id}
            """
          ).on(
            'id -> this.id,
            'name -> this.name,
            'posted -> this.posted,
            'poster -> this.poster.id.get,
            'location -> this.location.id.get
          ).executeUpdate()

          // Save the properties
          val newProperties = this.properties.map(p => Property(p.id, p.attribute, p.value, this.objId).save)

          // Save the location
          val newLocation = this.location.save

          // Don't save the user
          // Return the posting
          Posting(this.id, this.name, this.posted, this.poster, newLocation, newProperties, this.objId)
        } else {
          val newLocation = this.location.save

          // Save the posting
          val id: Option[Long] = SQL(
            """
            insert into posting (name, posted, poster, location)
            values ({name}, {posted}, {poster}, {location})
            """
          ).on(
            'name -> this.name,
            'posted -> this.posted,
            'poster -> this.poster.id.get,
            'location -> newLocation.id.get
          ).executeInsert()

          // Save the ViktyoObject
          val obj = ViktyoObject(NotAssigned, 'posting, id.get).create

          // Save the properties
          val newProperties = this.properties.map(p => Property(p.id, p.attribute, p.value, obj.id.get).save)

          // Return the posting
          Posting(Id(id.get), this.name, this.posted, this.poster, newLocation, newProperties, obj.id.get)
        }
    }
  }

  def delete() {
    DB.withConnection {
      implicit connection =>
        this.properties.map(p => p.delete())
        SQL("delete from posting where id = {id}").on('id -> this.id.get).executeUpdate()
        SQL("delete from viktyo_object where id = {id}").on('id -> this.objId)
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

  def setProperty(attribute: String, value: String): Posting = {
    Posting(this.id, this.name, this.posted, this.poster, this.location,
      this.properties.filterNot(p => p.attribute == attribute) + Property(NotAssigned, attribute, value, 0), this.objId)
  }

  def removeProperty(attribute: String): Posting = {
    Posting(this.id, this.name, this.posted, this.poster, this.location,
      this.properties.filterNot(p => p.attribute == attribute), this.objId)
  }
}

object Posting {
  val simple = {
    get[Pk[Long]]("posting.id") ~
      get[String]("posting.name") ~
      get[Long]("posting.posted") ~
      get[Long]("posting.poster") ~
      get[Long]("posting.location") ~
      get[Long]("object.id") map {
      case id ~ name ~ posted ~ poster ~ location ~ objId =>
        Posting(id, name, posted, User.findById(poster).get, Location.findById(location).get,
          Property.listByObjId(objId), objId)
    }
  }

  def findById(id: Long): Option[Posting] = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
          SELECT object.id, posting. *
          FROM object
          JOIN posting ON ( posting.id = object.objId )
          WHERE object.objType = {postingType}
          AND posting.id = {id}
          """
        ).on(
          'postingType -> ViktyoObject.typeMap('posting),
          'id -> id
        ).as(Posting.simple.singleOpt)
    }
  }

  def list: List[Posting] = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
          SELECT object.id, posting.*
          FROM object
          JOIN posting ON ( posting.id = object.objId )
          WHERE object.objType = {postingType}
          """
        ).on(
          'postingType -> ViktyoObject.typeMap('posting)
        ).as(Posting.simple *)
    }
  }

  def listAll: List[Posting] = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
          SELECT object.id, posting.*
          FROM object
          JOIN posting ON ( posting.id = object.objId )
          WHERE object.objType = {postingType}
          """
        ).on(
          'postingType -> ViktyoObject.typeMap('posting)
        ).as(Posting.simple *)
    }
  }

}
