package controllers

import play.api._
import play.api.mvc._
import models._
import anorm.NotAssigned
import java.util.Date
import org.apache.commons.lang3.StringEscapeUtils
import tools.Emailer

object Application extends Controller {
  
  def index = Action {

//    Emailer.sendEmail("Josh Monson", "camman3d@gmail.com", "Test email from VIKTYO", "This is a second test.")

    Ok // TODO: Create view
  }
  
}