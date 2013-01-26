package controllers

import play.api.mvc.{Action, Controller}
import models.ViktyoConfiguration

/**
 * Created with IntelliJ IDEA.
 * User: camman3d
 * Date: 1/25/13
 * Time: 5:17 PM
 * To change this template use File | Settings | File Templates.
 */
class Admin extends Controller {

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
        Ok // TODO: Unauthorized with success message
  }

}
