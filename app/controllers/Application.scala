package controllers

import play.api._
import play.api.mvc._
import models._
import anorm.NotAssigned
import java.util.Date

object Application extends Controller {
  
  def index = Action {
    //Ok(views.html.index("Your new application is ready."))

    //val property = Property(NotAssigned, "one", "two",)

    //val user = User(NotAssigned, "Josh Monson", "camman3d", "pass").setProperty("one", "two").setProperty("three", "four").save
    val user = User.findById(5).get

    //val posting = Posting(NotAssigned, "Intership!", 123456, user, Location(NotAssigned, "South Jordan", 5.6, 7.8)).save
    //val posting = Posting.findById(2)

    //val image = Image(NotAssigned, "Some Image", "http://example.com/img.jpg").setProperty("color", "greyscale").save
//    val image = Image.findById(2)

//    val text = Text(NotAssigned, "Hey there").save
    val text = Text.findById(1).get

    val as = ActivityStream(NotAssigned, new Date().getTime, user, "posted", text.objId, user.objId).save

    Ok(as.toString)
  }
  
}