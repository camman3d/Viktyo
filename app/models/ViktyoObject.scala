package models

import anorm._
import play.api.db.DB
import play.api.Play.current

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
    'user -> 1,
    'posting -> 2
  )
}