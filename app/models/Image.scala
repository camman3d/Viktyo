package models

import anorm._
import play.api.db.DB
import play.api.Play.current
import anorm.SqlParser._
import anorm.~
import tools.ImageUploader

case class Image(
  id: Pk[Long],
  name: String,
  uri: String, // Location
  properties: Set[Property] = Set(),
  objId: Long = 0
) {
  def save: Image = {
    DB.withConnection { implicit connection =>
      if (this.id.isDefined) {
        // Save the image
        SQL(
          """
            update image
            set name = {name}, uri = {uri}
            where id = {id}
          """
        ).on(
          'id -> this.id,
          'name -> this.name,
          'uri -> this.uri
        ).executeUpdate()

        // Save the properties
        val newProperties = this.properties.map(p => Property(p.id, p.attribute, p.value, this.objId).save)

        // Return the posting
        Image(this.id, this.name, this.uri, newProperties, this.objId)
      } else {
        // Save the image
        val id: Option[Long] = SQL(
          """
            insert into image (name, uri)
            values ({name}, {uri})
          """
        ).on(
          'name -> this.name,
          'uri -> this.uri
        ).executeInsert()

        // Save the ViktyoObject
        val obj = ViktyoObject(NotAssigned, 'image, id.get).create

        // Save the properties
        val newProperties = this.properties.map(p => Property(p.id, p.attribute, p.value, obj.id.get).save)

        // Return the posting
        Image(Id(id.get), this.name, this.uri, newProperties, obj.id.get)
      }
    }
  }

  def delete() {
    DB.withConnection { implicit connection =>
      // TODO: Delete the file
      this.properties.map(p => p.delete())
      SQL("delete from image where id = {id}").on('id -> this.id.get).executeUpdate()
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

  def setProperty(attribute: String, value: String): Image = {
    Image(this.id, this.name, this.uri,
      this.properties.filterNot(p => p.attribute == attribute) + Property(NotAssigned, attribute, value, 0), this.objId)
  }

  def removeProperty(attribute: String): Image = {
    Image(this.id, this.name, this.uri, this.properties.filterNot(p => p.attribute == attribute), this.objId)
  }

  // Owner property
  def getOwner: User = User.findById(getProperty("owner").get.toLong).get

}

object Image {
  val simple = {
    get[Pk[Long]]("image.id") ~
      get[String]("image.name") ~
      get[String]("image.uri") ~
      get[Long]("object.id") map {
      case id~name~uri~objId => Image(id, name, uri, Property.listByObjId(objId), objId)
    }
  }

  def findById(id: Long): Option[Image] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          SELECT object.id, image. *
          FROM object
          JOIN image ON ( image.id = object.objId )
          WHERE object.objType = {imageType}
          AND image.id = {id}
        """
      ).on(
        'imageType -> ViktyoObject.typeMap('image),
        'id -> id
      ).as(Image.simple.singleOpt)
    }
  }

  def findByObjId(id: Long): Option[Image] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          SELECT object.id, image. *
          FROM object
          JOIN image ON ( image.id = object.objId )
          WHERE object.id = {id}
        """
      ).on(
        'id -> id
      ).as(Image.simple.singleOpt)
    }
  }
}
