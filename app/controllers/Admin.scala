package controllers

import play.api.mvc.{Action, Controller}
import models.{User, ViktyoConfiguration}

/**
 * Created with IntelliJ IDEA.
 * User: camman3d
 * Date: 1/25/13
 * Time: 5:17 PM
 * To change this template use File | Settings | File Templates.
 */
object Admin extends Controller {

  def dashboard = Action {
    implicit request =>

    // Check that the user is logged in and is an admin
      val user = Account.getCurrentUser
      if (user.isDefined && user.get.getProperty("accountType").get == "admin") {
        Ok // TODO: Create view

      } else // Not authorized
        Redirect(routes.Application.index()).flashing("alert" -> "You are not authorized to view that page.")
  }

  def configure = Action {
    implicit request =>

    // Check that the user is logged in and is an admin
      val user = Account.getCurrentUser
      if (user.isDefined && user.get.getProperty("accountType").get == "admin") {
        val configurations = ViktyoConfiguration.list
        Ok // TODO: Create view

      } else // Not authorized
        Redirect(routes.Application.index()).flashing("alert" -> "You are not authorized to view that page.")
  }

  def setConfiguration() = Action(parse.urlFormEncoded) {
    implicit request =>
    // Check that the user is logged in and is an admin
      val user = Account.getCurrentUser
      if (user.isDefined && user.get.getProperty("accountType").get == "admin") {
        val configuration = ViktyoConfiguration.findById(request.body("configuration")(0).toLong)
        configuration.get.setData(request.body("configuration")(0)).save
        Ok // TODO: Redirect with success message

      } else // Not authorized
        Redirect(routes.Application.index()).flashing("alert" -> "You are not authorized to view that page.")
  }

  def users = Action {
    implicit request =>
      // Check that the user is logged in and is an admin
      val user = Account.getCurrentUser
      if (user.isDefined && user.get.getProperty("accountType").get == "admin") {
        val page = request.queryString.get("page").getOrElse(Seq("0"))(0).toInt
        val users = User.list(page)
        Ok // TODO: Redirect with success message

      } else // Not authorized
        Redirect(routes.Application.index()).flashing("alert" -> "You are not authorized to view that page.")
  }

  def toggleAdmin(id: Long) = Action {
    implicit request =>
    // Check that the user is logged in and is an admin
      val user = Account.getCurrentUser
      if (user.isDefined && user.get.getProperty("accountType").get == "admin") {
        val otherUser = User.findById(id).get
        if (otherUser.getProperty("accountType").get == "admin")
          otherUser.setProperty("accountType", "user").save
        else
          otherUser.setProperty("accountType", "admin").save
        Ok // TODO: Redirect with success message

      } else // Not authorized
        Redirect(routes.Application.index()).flashing("alert" -> "You are not authorized to view that page.")
  }

  def deleteUser(id: Long) = Action {
    implicit request =>
    // Check that the user is logged in and is an admin
      val user = Account.getCurrentUser
      if (user.isDefined && user.get.getProperty("accountType").get == "admin") {
        val otherUser = User.findById(id).get
        otherUser.delete()
        Ok // TODO: Redirect with success message

      } else // Not authorized
        Redirect(routes.Application.index()).flashing("alert" -> "You are not authorized to view that page.")
  }

}
