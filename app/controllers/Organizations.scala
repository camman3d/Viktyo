package controllers

import play.api.mvc.Controller
import models.User
import play.api.libs.json.JsArray

/**
 * Created with IntelliJ IDEA.
 * User: Josh
 * Date: 1/24/13
 * Time: 2:57 PM
 * To change this template use File | Settings | File Templates.
 */
object Organizations extends Controller {

  def browse(view: Symbol) = Account.AuthenticatedAction {
    implicit request =>
      implicit user =>
        val organizations = User.listByAccountType("organization")
        val organizationsJson = JsArray(organizations.map(_.toJson)).toString()
        if (view == 'list)
          Ok(views.html.organizations.browseList(organizationsJson))
        else
          Ok // TODO: Create map view
  }

  def browseList = browse('list)

  def browseMap = browse('map)

  def view(id: Long) = Account.AuthenticatedAction {
    implicit request =>
      implicit user =>

      // Make sure the org exists
        val organization = User.findById(id)
        if (organization.isDefined && organization.get.getProperty("accountType") == "organization") {
          Ok // TODO: Create view

        } else // Organization doesn't exist
          Ok // TODO: Redirect with message
  }
}
