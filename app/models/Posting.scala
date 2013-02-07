package models

import anorm._
import play.api.db.DB
import play.api.Play.current
import anorm.SqlParser._
import anorm.~
import play.api.libs.json.{JsValue, Json}
import org.apache.commons.lang3.StringUtils
import play.api.mvc.{Request, AnyContent}
import java.util.Date

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

  def toJson = Json.obj(
    "id" -> this.id.get,
    "name" -> this.name,
    "location" -> this.location.toJson,
    "poster" -> this.poster.toJson,
    "coverPicture" -> this.getCoverPictureUrl,
    "type" -> this.getProperty("postingType").get,
    "panoramio" -> this.getPanoramio,
    "description" -> this.getDescription,
    "followers" -> this.countFavorites,
    "favorites" -> this.countFavorites,
    "views" -> this.getViews
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

  def setProperty(attribute: String, value: String): Posting = {
    Posting(this.id, this.name, this.posted, this.poster, this.location,
      this.properties.filterNot(p => p.attribute == attribute) + Property(NotAssigned, attribute, value, 0), this.objId)
  }

  def removeProperty(attribute: String): Posting = {
    Posting(this.id, this.name, this.posted, this.poster, this.location,
      this.properties.filterNot(p => p.attribute == attribute), this.objId)
  }

  def getCoverPicture: Option[String] = getProperty("coverPicture")

  def getCoverPictureUrl: String = getCoverPicture.getOrElse("/assets/images/postings/orange_world.jpg")

  def getPanoramio: Option[JsValue] = getProperty("panoramio").map(Json.parse(_))

  // Generic Property List functions

  def getPropertyList(attribute: String): List[Long] =
    this.getProperty(attribute).map(_.split(",").map(s => s.toLong).toList).getOrElse(List())

  def addToPropertyList(attribute: String, value: Long): Posting = {
    if (this.getProperty(attribute).isDefined)
      this.setProperty(attribute, this.getProperty(attribute).get + "," + value)
    else
      this.setProperty(attribute, value.toString)
  }

  def removeFromPropertyList(attribute: String, value: Long): Posting = {
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

  def countPropertyList(attribute: String): Int = getProperty(attribute).map(_.split(",")).size

  // Followers

  def getFollowers: List[User] = getPropertyList("followers").map(n => User.findById(n).get)

  def addFollower(user: User): Posting = addToPropertyList("followers", user.id.get)

  def removeFollower(user: User): Posting = removeFromPropertyList("followers", user.id.get)

  def hasFollower(user: User): Boolean = hasInPropertyList("followers", user.id.get)

  def countFollowers = countPropertyList("followers")

  // Favorites

  def getFavorites: List[User] = getPropertyList("favorites").map(n => User.findById(n).get)

  def addFavorite(user: User): Posting = addToPropertyList("favorites", user.id.get)

  def removeFavorite(user: User): Posting = removeFromPropertyList("favorites", user.id.get)

  def hasFavorite(user: User): Boolean = hasInPropertyList("favorites", user.id.get)

  def countFavorites = countPropertyList("favorites")

  // Views

  def getViews: Int = getProperty("views").map(_.toInt).getOrElse(0)

  def incrementViews: Posting = this.setProperty("views", (getViews + 1).toString)

  // Description

  def getDescription: String = getProperty("description").getOrElse("No description has been written yet.")

  // Type

  def getType: String = StringUtils.capitalize(getProperty("postingType").get.replaceAll("_", " "))

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


  def findByObjId(id: Long): Option[Posting] = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
          SELECT object.id, posting. *
          FROM object
          JOIN posting ON ( posting.id = object.objId )
          WHERE object.id = {id}
          """
        ).on(
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

  def search(searchString: String): List[Posting] = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
          SELECT object.id, posting.*
          FROM object
          JOIN posting ON ( posting.id = object.objId )
          left JOIN property ON ( object.id = property.objId )
          join location on ( posting.location = location.id )
          WHERE object.objType = 2
          AND (posting.name COLLATE UTF8_GENERAL_CI LIKE {search}
          OR (property.attribute = "description" AND property.value COLLATE UTF8_GENERAL_CI LIKE {search})
          OR location.name COLLATE UTF8_GENERAL_CI LIKE {search})
          """
        ).on(
          'userType -> ViktyoObject.typeMap('posting),
          'search -> ("%" + searchString + "%")
        ).as(Posting.simple *)
    }
  }

  def createFromRequest()(implicit request: Request[AnyContent], poster: User): Posting = {
    val params = request.body.asFormUrlEncoded.get
    val name = params("name")(0)
    val posted = new Date().getTime
    val locationName = params("location")(0)
    val latitude = params("latitude")(0).toDouble
    val longitude = params("longitude")(0).toDouble
    val location = Location(NotAssigned, locationName, latitude, longitude)
    val postingType = params("postingType")(0)

    Posting(NotAssigned, name, posted, poster, location).setProperty("postingType", postingType)
  }

}
