package controllers

import play.api.mvc.{Action, Controller}
import models.{User, Network}

/**
 * Created with IntelliJ IDEA.
 * User: Josh
 * Date: 1/24/13
 * Time: 2:57 PM
 * To change this template use File | Settings | File Templates.
 */
object Organizations extends Controller {

  def browse(view: Symbol) = Action { implicit request =>

    // Check that the user is logged in
    val user = Account.getCurrentUser
    if (user.isDefined) {
      val organizations = User.listByAccountType("organization")
      if (view == 'list)
        Ok // TODO: Create list view
      else
        Ok // TODO: Create map view

    } else // User not logged in
      Redirect(routes.Application.index()).flashing("alert" -> "You are not logged in")
  }

  def browseList = browse('list)
  def browseMap = browse('map)

  def view(id: Long) = Action { implicit request =>

    // Check that the user is logged in
    val user = Account.getCurrentUser
    if (user.isDefined) {

      // Make sure the org exists
      val organization = User.findById(id)
      if (organization.isDefined && organization.get.getProperty("accountType") == "organization") {
        Ok // TODO: Create view

      } else // Organization doesn't exist
        Ok // TODO: Redirect with message
    } else // User not logged in
      Redirect(routes.Application.index()).flashing("alert" -> "You are not logged in")
  }
}
