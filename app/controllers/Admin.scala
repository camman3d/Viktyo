package controllers

import play.api.mvc._
import models.{User, ViktyoConfiguration}

/**
 * Created with IntelliJ IDEA.
 * User: camman3d
 * Date: 1/25/13
 * Time: 5:17 PM
 * To change this template use File | Settings | File Templates.
 */
object Admin extends Controller {

  // Authentication helper
  def AdminAuthenticatedAction(f: Request[AnyContent] => (User => Result)) = {
    Action {
      implicit request =>
        val user = Account.getCurrentUser
        if (user.isDefined && user.get.getProperty("accountType").get == "admin")
          f(request)(user.get)
        else // User not logged in
          Redirect(routes.Application.index()).flashing("alert" -> "You are not logged in")
    }
  }

  def dashboard = AdminAuthenticatedAction {
    implicit request =>
      implicit user =>
        Ok(views.html.admin.dashboard())
  }

  def configure = AdminAuthenticatedAction {
    implicit request =>
      implicit user =>

      // Get the configurations
        val configurations = ViktyoConfiguration.list
        val userSignupRequiredFields = configurations.find(_.name == "signup.user.requiredFields").get.data.right.get
        val userSignupFields = configurations.find(_.name == "signup.user.availableFields").get.data.right.get.zip(
          configurations.find(_.name == "signup.user.availableFieldsTypes").get.data.right.get
        ).map(field => (field._1, field._2, userSignupRequiredFields.contains(field._1)))

        Ok(views.html.admin.configure(userSignupFields))

  }

  def setConfiguration() = AdminAuthenticatedAction {
    implicit request =>
      implicit user =>
        val configuration = ViktyoConfiguration.findById(request.body.asFormUrlEncoded.get("configuration")(0).toLong)
        configuration.get.setData(request.body.asFormUrlEncoded.get("configuration")(0)).save
        Redirect(routes.Admin.configure()).flashing("success" -> "Configuration saved")
  }

  def users = AdminAuthenticatedAction {
    implicit request =>
      implicit user =>
        val page = request.queryString.get("page").getOrElse(Seq("0"))(0).toInt
        val users = User.list(page)
        Ok // TODO: Redirect with success message

  }

  def toggleAdmin(id: Long) = AdminAuthenticatedAction {
    implicit request =>
      implicit user =>
        val otherUser = User.findById(id).get
        if (otherUser.getProperty("accountType").get == "admin")
          otherUser.setProperty("accountType", "user").save
        else
          otherUser.setProperty("accountType", "admin").save
        Ok // TODO: Redirect with success message
  }

  def deleteUser(id: Long) = AdminAuthenticatedAction {
    implicit request =>
      implicit user =>
        val otherUser = User.findById(id).get
        otherUser.delete()
        Ok // TODO: Redirect with success message
  }


  def pages = TODO

  def listings = TODO

  def networks = TODO

  def stats = TODO

  def finance = TODO

}
