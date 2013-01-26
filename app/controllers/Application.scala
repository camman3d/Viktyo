package controllers

import play.api._
import libs.json.{JsArray, Json}
import play.api.mvc._
import models._
import anorm.NotAssigned
import java.util.Date
import org.apache.commons.lang3.StringEscapeUtils
import tools.{Hasher, EdgeRank, Emailer}

object Application extends Controller {
  
  def index = Action { implicit  request =>

//    Emailer.sendEmail("Josh Monson", "camman3d@gmail.com", "Test email from VIKTYO", "This is a second test.")
//    val user = User.findById(5).get
//    val feed = EdgeRank.getFeed(user)
//    val as = ActivityStream.createStatusUpdate(user, "this is my update").save


//    Ok(feed.toString) // TODO: Create view

//    val user = User(NotAssigned, "Josh", "camman3d", Hasher.sha256Base64("Fr-21j0$h")).save

//    Ok
    Ok(views.html.index())
  }

  def search = Action(parse.urlFormEncoded) { implicit request =>
//    try {
      val searchString = request.body("search")(0)
      val allUsers = User.search(searchString)
      val users = allUsers.filter(u => {
        val atype = u.getProperty("accountType").get
        atype == "user" || atype == "admin"
      })
      val organizations = allUsers.filter(u => u.getProperty("accountType").get == "organization")
      val postings = Posting.search(searchString)



      Ok(Json.obj(
        "users" -> JsArray(users.map(u => u.toJson)),
        "postings" -> JsArray(postings.map(p => p.toJson)),
        "organizations" -> JsArray(organizations.map(u => u.toJson))
      ))

//    } catch {
//      case _ => BadRequest(Json.obj("success" -> false))
//    }
  }
  
}