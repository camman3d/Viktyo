package models

import anorm._
import play.api.db._
import play.api.Play.current
import anorm.Id
import anorm.SqlParser._
import play.api.libs.json._
import play.api.libs.json.util._
import play.api.libs.json.Writes._

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

  def toJson: JsObject =
    Json.obj(
      "id" -> this.id.get,
      "username" -> this.username,
      "fullname" -> this.fullname
    )

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

  // Generic Property List functions

  def getPropertyList(attribute: String): List[Long] = {
    if (this.getProperty(attribute).isDefined)
      this.getProperty(attribute).get.split(",").map(s => s.toLong).toList
    else
      List()
  }

  def addToPropertyList(attribute: String, value: Long): User = {
    if (this.getProperty(attribute).isDefined)
      this.setProperty(attribute, this.getProperty(attribute).get + "," + value)
    else
      this.setProperty(attribute, value.toString)
  }

  def removeFromPropertyList(attribute: String, value: Long): User = {
    if (this.getProperty(attribute).isDefined)
      this.setProperty(attribute,
        this.getProperty(attribute).get.split(",").filterNot(p => p == value.toString).mkString(","))
    else
      this
  }

  def hasInPropertyList(attribute: String, value: Long): Boolean = {
    if (this.getProperty(attribute).isDefined)
      this.getProperty(attribute).get.split(",").map(n => n.toLong).filter(l => l == value).size > 0
    else
      false
  }

  // Network functions
  def getNetworks: List[Network] = getPropertyList("networks").map(n => Network.findById(n).get)

  def addNetwork(network: Network): User = addToPropertyList("networks", network.id.get)

  def removeNetwork(network: Network): User = removeFromPropertyList("networks", network.id.get)

  def hasNetwork(network: Network): Boolean = hasInPropertyList("networks", network.id.get)

  // Favorite functions
  def getFavorites: List[Posting] = getPropertyList("favorites").map(n => Posting.findById(n).get)

  def addFavorite(posting: Posting): User = addToPropertyList("favorites", posting.id.get)

  def removeFavorite(posting: Posting): User = removeFromPropertyList("favorites", posting.id.get)

  def hasFavorite(posting: Posting): Boolean = hasInPropertyList("favorites", posting.id.get)

  // Following functions
  def getFollowing: List[User] = getPropertyList("following").map(n => User.findById(n).get)

  def addFollowing(user: User): User = addToPropertyList("following", user.id.get)

  def removeFollowing(user: User): User = removeFromPropertyList("following", user.id.get)

  def hasFollowing(user: User): Boolean = hasInPropertyList("following", user.id.get)

  // Follower functions
  def getFollower: List[User] = getPropertyList("followers").map(n => User.findById(n).get)

  def addFollower(user: User): User = addToPropertyList("followers", user.id.get)

  def removeFollower(user: User): User = removeFromPropertyList("followers", user.id.get)

  def hasFollower(user: User): Boolean = hasInPropertyList("followers", user.id.get)
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

  def listByProperty(attribute: String, value: String): List[User] = {
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
        ).as(User.simple *)
    }
  }

  def listByAccountType(accountType: String): List[User] =
    listByProperty("accountType", accountType)

  def list(page: Integer = 0, pageSize: Integer = 10): List[User] = {
    val offset = page * pageSize
    DB.withConnection {
      implicit connection =>
        SQL(
          """
          SELECT object.id, user.*
          FROM object
          JOIN user ON ( user.id = object.objId )
          WHERE object.objType = {userType}
          limit {pageSize} offset {offset}
          """
        ).on(
          'userType -> ViktyoObject.typeMap('user),
          'pageSize -> pageSize,
          'offset -> offset
        ).as(User.simple *)
    }
  }

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
    DB.withConnection {
      implicit connection =>
        val count = SQL("SELECT count(*) FROM user WHERE username = {username}").on('username -> username).as(scalar[Long].single)
        count > 0
    }
  }

  def search(searchString: String): List[User] = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
          SELECT object.id, user.*
          FROM object
          JOIN user ON ( user.id = object.objId )
          WHERE object.objType = {userType}
          AND (user.username COLLATE UTF8_GENERAL_CI LIKE {username}
          OR user.fullname COLLATE UTF8_GENERAL_CI LIKE {fullname})
          """
        ).on(
          'userType -> ViktyoObject.typeMap('user),
          'username -> ("%" + searchString + "%"),
          'fullname -> ("%" + searchString + "%")
        ).as(User.simple *)
    }
  }

}
