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
    DB.withConnection {
      implicit connection =>
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

  def delete() {
    DB.withConnection {
      implicit connection =>
        this.properties.map(p => p.delete())
        SQL("delete from user where id = {id}").on('id -> this.id.get).executeUpdate()
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

  def setProperty(attribute: String, value: String): User = {
    User(this.id, this.fullname, this.username, this.password,
      this.properties.filterNot(p => p.attribute == attribute) + Property(NotAssigned, attribute, value, 0), this.objId)
  }

  def removeProperty(attribute: String): User = {
    User(this.id, this.fullname, this.username, this.password, this.properties.filterNot(p => p.attribute == attribute), this.objId)
  }

  def setFullname(fullname: String): User = {
    User(this.id, fullname, this.username, this.password, this.properties, this.objId)
  }

  def setUsername(username: String): User = {
    User(this.id, this.fullname, username, this.password, this.properties, this.objId)
  }

  def setPassword(password: String): User = {
    User(this.id, this.fullname, this.username, password, this.properties, this.objId)
  }
}

object User {

  val simple = {
    get[Pk[Long]]("user.id") ~
      get[String]("user.fullname") ~
      get[String]("user.username") ~
      get[String]("user.password") ~
      get[Long]("object.id") map {
      case id ~ fullname ~ username ~ password ~ objId => User(id, fullname, username, password, Property.listByObjId(objId), objId)
    }
  }

  def findById(id: Long): Option[User] = {
    DB.withConnection {
      implicit connection =>
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

  def findByUsername(username: String): Option[User] = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
          SELECT object.id, user. *
          FROM object
          JOIN user ON ( user.id = object.objId )
          WHERE object.objType = {userType}
          AND user.username = {username}
          """
        ).on(
          'userType -> ViktyoObject.typeMap('user),
          'username -> username
        ).as(User.simple.singleOpt)
    }
  }

  def findByProperty(attribute: String, value: String): Option[User] = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
          SELECT object.id, user.*
          FROM object
          JOIN user ON ( user.id = object.objId )
          JOIN property ON ( object.id = property.objId)
          WHERE object.objType = {userType}
          AND property.attribute = {attribute}
          AND property.value = {value}
          """
        ).on(
          'userType -> ViktyoObject.typeMap('user),
          'attribute -> attribute,
          'value -> value
        ).as(User.simple.singleOpt)
    }
  }

  def listByProperty(attribute: String, value: String, page: Int = 0, pageSize: Int = 10): List[User] = {
    val offset = page * pageSize
    DB.withConnection {
      implicit connection =>
        SQL(
          """
          SELECT object.id, user.*
          FROM object
          JOIN user ON ( user.id = object.objId )
          JOIN property ON ( object.id = property.objId)
          WHERE object.objType = {userType}
          AND property.attribute = {attribute}
          AND property.value = {value}
            |limit {pageSize} offset {offset}
          """
        ).on(
          'userType -> ViktyoObject.typeMap('user),
          'attribute -> attribute,
          'value -> value,
          'pageSize -> pageSize,
          'offset -> offset
        ).as(User.simple *)
    }
  }

  def listByAccountType(accountType: String, page: Int = 0, pageSize: Int = 10): List[User] =
    listByProperty("accountType", accountType, page, pageSize)

  def authenticate(username: String, password: String): Option[User] = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
          SELECT object.id, user. *
          FROM object
          JOIN user ON ( user.id = object.objId )
          WHERE object.objType = {userType}
          AND user.username = {username}
          AND user.password = {password}
          """
        ).on(
          'userType -> ViktyoObject.typeMap('user),
          'username -> username,
          'password -> password
        ).as(User.simple.singleOpt)
    }
  }

  def checkUsername(username: String): Boolean = {
    DB.withConnection { implicit connection =>
      val count = SQL("SELECT count(*) FROM user WHERE username = {username}").on('username -> username).as(scalar[Long].single)
      count > 0
    }
  }
}
