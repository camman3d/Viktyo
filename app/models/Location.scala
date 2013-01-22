package models

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current

case class Location(
                     id: Pk[Long],
                     name: String,
                     latitude: Double,
                     longitude: Double
                     ) {
  def save: Location = {
    DB.withConnection {
      implicit connection =>
        val count = SQL("SELECT count(*) FROM location WHERE name = {name}").on('name -> this.name).as(scalar[Long].single)
        if (count > 0)
          Location.findByName(this.name).get
        else {
          val id: Option[Long] = SQL(
            """
            insert into location
            (name, latitude, longitude)
            values ({name}, {latitude}, {longitude})
            """
          ).on(
            'name -> this.name,
            'latitude -> this.latitude,
            'longitude -> this.longitude
          ).executeInsert()
          Location(Id(id.get), this.name, this.latitude, this.longitude)
        }
    }
  }

}

object Location {
  val simple = {
    get[Pk[Long]]("location.id") ~
      get[String]("location.name") ~
      get[Double]("location.latitude") ~
      get[Double]("location.longitude") map {
      case id ~ name ~ latitude ~ longitude => Location(id, name, latitude, longitude)
    }
  }

  def findById(id: Long): Option[Location] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from location where id = {id}").on('id -> id).as(Location.simple.singleOpt)
    }
  }

  def findByName(name: String): Option[Location] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from location where name = {name}").on('name -> name).as(Location.simple.singleOpt)
    }
  }
}
