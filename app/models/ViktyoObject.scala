package models

import anorm._
import play.api.db.DB
import play.api.Play.current
import anorm.SqlParser._
import anorm.~
import anorm.Id

case class ViktyoObject(
  id: Pk[Long],
  objType: Symbol,
  objId: Long
) {
  def create: ViktyoObject = {
    DB.withConnection { implicit connection =>
      val id: Option[Long] = SQL(
        """
          insert into object (objType, objId)
          values ({objType}, {objId})
        """
      ).on(
        'objType -> ViktyoObject.typeMap(this.objType),
        'objId -> this.objId
      ).executeInsert()
      ViktyoObject(Id(id.get), this.objType, this.objId)
    }
  }
}

object ViktyoObject {
  val typeMap = Map[Symbol, Int](
    'none -> 0,
    'user -> 1,
    'posting -> 2,
    'image -> 3,
    'text -> 4,
    'network -> 5
  )

  val typeReverseMap = typeMap.map(_.swap)

  val simple = {
    get[Pk[Long]]("object.id") ~
      get[Int]("object.objType") ~
      get[Long]("object.objId") map {
      case id ~ objType ~ objId => ViktyoObject(id, typeReverseMap(objType), objId)
    }
  }

  def findById(id: Long): Option[ViktyoObject] = {
    DB.withConnection {
      implicit connection =>
        SQL("SELECT * FROM object WHERE id = {id}").on(
          'id -> id
        ).as(ViktyoObject.simple.singleOpt)
    }
  }

  def getObjectType(id: Long): Symbol = {
    val obj = findById(id)
    if (obj.isDefined)
      obj.get.objType
    else
      'none
  }
}