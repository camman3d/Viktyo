package models

import anorm._
import play.api.db.DB
import play.api.Play.current
import anorm.SqlParser._
import anorm.~
import org.apache.commons.lang3.StringEscapeUtils

case class ViktyoConfiguration(
  id: Pk[Long],
  name: String,
  data: Either[String, List[String]]
) {

  def getDataType: String = {
    if (data.isLeft)
      "single"
    else
      "multiple"
  }

  def getDataString: String = {
    if (data.isLeft)
      data.left.get
    else
      data.right.get.map(s => StringEscapeUtils.escapeCsv(s)).mkString(",")
  }

  def save: ViktyoConfiguration = {
    DB.withConnection { implicit connection =>
      if (this.id.isDefined) {
        // Save the configuration
        SQL(
          """
            update configuration
            set name = {name}, dataType = {dataType}, data: {data}
            where id = {id}
          """
        ).on(
          'id -> this.id,
          'name -> this.name,
          'dataType -> this.getDataType,
          'data -> this.getDataString
        ).executeUpdate()

        // Return the configuration
        ViktyoConfiguration(this.id, this.name, this.data)
      } else {
        // Save the configuration
        val id: Option[Long] = SQL(
          """
            insert into configuration (name, dataType, data)
            values ({name}, {dataType}, {data})
          """
        ).on(
          'name -> this.name,
          'name -> this.name,
          'dataType -> this.getDataType,
          'data -> this.getDataString
        ).executeInsert()

        // Return the configuration
        ViktyoConfiguration(Id(id.get), this.name, this.data)
      }
    }
  }
}

object ViktyoConfiguration {
  def buildDataFromString(dataType: String, data: String): Either[String, List[String]] = {
    if (dataType == "single")
      Left(data)
    else
      Right(data.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)").map(s => StringEscapeUtils.unescapeCsv(s)).toList)
  }

  val simple = {
    get[Pk[Long]]("configuration.id") ~
      get[String]("configuration.name") ~
      get[String]("configuration.dataType") ~
      get[String]("configuration.data") map {
      case id~name~dataType~data => ViktyoConfiguration(id, name, buildDataFromString(dataType, data))
    }
  }

  def findById(id: Long): Option[ViktyoConfiguration] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT * from configuration where id = {id}").on('id -> id).as(ViktyoConfiguration.simple.singleOpt)
    }
  }

  def findByName(name: String): Option[ViktyoConfiguration] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT * from configuration where name = {name}").on('name -> name).as(ViktyoConfiguration.simple.singleOpt)
    }
  }
}
