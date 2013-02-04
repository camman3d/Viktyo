package models

import anorm._
import play.api.db.DB
import play.api.Play.current
import anorm.SqlParser._
import anorm.~

case class Text(
                  id: Pk[Long],
                  textValue: String,
                  properties: Set[Property] = Set(),
                  objId: Long = 0
                  ) {
  def save: Text = {
    DB.withConnection { implicit connection =>
      if (this.id.isDefined) {
        // Save the text
        SQL(
          """
            update text
            set textValue = {textValue}
            where id = {id}
          """
        ).on(
          'id -> this.id,
          'textValue -> this.textValue
        ).executeUpdate()

        // Save the properties
        val newProperties = this.properties.map(p => Property(p.id, p.attribute, p.value, this.objId).save)

        // Return the text
        Text(this.id, this.textValue, newProperties, this.objId)
      } else {
        // Save the text
        val id: Option[Long] = SQL(
          """
            insert into text (textValue)
            values ({textValue})
          """
        ).on(
          'textValue -> this.textValue
        ).executeInsert()

        // Save the ViktyoObject
        val obj = ViktyoObject(NotAssigned, 'text, id.get).create

        // Save the properties
        val newProperties = this.properties.map(p => Property(p.id, p.attribute, p.value, obj.id.get).save)

        // Return the text
        Text(Id(id.get), this.textValue, newProperties, obj.id.get)
      }
    }
  }

  def delete() {
    DB.withConnection { implicit connection =>
      this.properties.map(p => p.delete())
      SQL("delete from text where id = {id}").on('id -> this.id.get).executeUpdate()
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

  def setProperty(attribute: String, value: String): Text = {
    Text(this.id, this.textValue,
      this.properties.filterNot(p => p.attribute == attribute) + Property(NotAssigned, attribute, value, 0), this.objId)
  }

  def removeProperty(attribute: String): Text = {
    Text(this.id, this.textValue, this.properties.filterNot(p => p.attribute == attribute), this.objId)
  }
}

object Text {
  val simple = {
    get[Pk[Long]]("text.id") ~
      get[String]("text.textValue") ~
      get[Long]("object.id") map {
      case id~textValue~objId => Text(id, textValue, Property.listByObjId(objId), objId)
    }
  }

  def findById(id: Long): Option[Text] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          SELECT object.id, text. *
          FROM object
          JOIN text ON ( text.id = object.objId )
          WHERE object.objType = {textType}
          AND text.id = {id}
        """
      ).on(
        'textType -> ViktyoObject.typeMap('text),
        'id -> id
      ).as(Text.simple.singleOpt)
    }
  }

  def findByObjId(id: Long): Option[Text] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          SELECT object.id, text. *
          FROM object
          JOIN text ON ( text.id = object.objId )
          WHERE object.id = {id}
        """
      ).on(
        'id -> id
      ).as(Text.simple.singleOpt)
    }
  }
}
