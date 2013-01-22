package controllers

import play.api._
import play.api.mvc._
import models.{Location, Posting, User, Property}
import anorm.NotAssigned

object Application extends Controller {
  
  def index = Action {
    //Ok(views.html.index("Your new application is ready."))

    //val property = Property(NotAssigned, "one", "two",)

    //val user = User(NotAssigned, "Josh Monson", "camman3d", "pass").setProperty("one", "two").setProperty("three", "four").save
    //val user = User.findById(5).get

    //val posting = Posting(NotAssigned, "Intership!", 123456, user, Location(NotAssigned, "South Jordan", 5.6, 7.8)).save
    val posting = Posting.findById(2)

    Ok(posting.toString)
  }
  
}