package models

import anorm._
import play.api.db.DB
import play.api.Play.current
import anorm.SqlParser._
import anorm.~

case class Network(
                  id: Pk[Long],
                  name: String,
                  description: String,
                  properties: Set[Property] = Set(),
                  objId: Long = 0
                  ) {
  def save: Network = {
    DB.withConnection { implicit connection =>
      if (this.id.isDefined) {
        // Save the image
        SQL(
          """
            update network
            set name = {name}, description = {description}
            where id = {id}
          """
        ).on(
          'id -> this.id,
          'name -> this.name,
          'description -> this.description
        ).executeUpdate()

        // Save the properties
        val newProperties = this.properties.map(p => Property(p.id, p.attribute, p.value, this.objId).save)

        // Return the posting
        Network(this.id, this.name, this.description, newProperties, this.objId)
      } else {
        // Save the image
        val id: Option[Long] = SQL(
          """
            insert into network (name, description)
            values ({name}, {description})
          """
        ).on(
          'name -> this.name,
          'description -> this.description
        ).executeInsert()

        // Save the ViktyoObject
        val obj = ViktyoObject(NotAssigned, 'network, id.get).create

        // Save the properties
        val newProperties = this.properties.map(p => Property(p.id, p.attribute, p.value, obj.id.get).save)

        // Return the posting
        Network(Id(id.get), this.name, this.description, newProperties, obj.id.get)
      }
    }
  }

  def delete() {
    DB.withConnection { implicit connection =>
      this.properties.map(p => p.delete())
      SQL("delete from network where id = {id}").on('id -> this.id.get).executeUpdate()
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

  def setProperty(attribute: String, value: String): Network = {
    Network(this.id, this.name, this.description,
      this.properties.filterNot(p => p.attribute == attribute) + Property(NotAssigned, attribute, value, 0), this.objId)
  }

  def removeProperty(attribute: String): Network = {
    Network(this.id, this.name, this.description, this.properties.filterNot(p => p.attribute == attribute), this.objId)
  }
}

object Network {
  val simple = {
    get[Pk[Long]]("network.id") ~
      get[String]("network.name") ~
      get[String]("network.description") ~
      get[Long]("object.id") map {
      case id~name~description~objId => Network(id, name, description, Property.listByObjId(objId), objId)
    }
  }

  def findById(id: Long): Option[Network] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          SELECT object.id, network. *
          FROM object
          JOIN network ON ( network.id = object.objId )
          WHERE object.objType = {networkType}
          AND network.id = {id}
        """
      ).on(
        'networkType -> ViktyoObject.typeMap('network),
        'id -> id
      ).as(Network.simple.singleOpt)
    }
  }
}
