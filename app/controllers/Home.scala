package controllers

import play.api.mvc.{Action, Controller}
import tools.{Hasher, FeedTools}
import models.{Network, Image, ActivityStream}
import anorm.NotAssigned
import java.util.Date
import tools.social.UserActions
import tools.images.ImageUploader

/**
 * Created with IntelliJ IDEA.
 * User: Josh
 * Date: 1/24/13
 * Time: 11:54 AM
 * To change this template use File | Settings | File Templates.
 */
object Home extends Controller {

  def addStatusUpdate() = Account.AuthenticatedAction {
    implicit request =>
      implicit user =>

      // Create the status update
        val statusUpdate = request.body.asFormUrlEncoded.get("statusUpdate")(0)
        UserActions.userPostsStatusUpdate(user, statusUpdate)
        Ok // TODO: Redirect with message
  }

  def addImage(imageId: Long) = Account.AuthenticatedAction {
    implicit request =>
      implicit user =>

        // Get the image and make sure you own it
        val image = Image.findById(imageId)
        if (image.isDefined && image.get.getProperty("owner").get == user.id.get.toString) {

          // Create the update
          UserActions.userPostsImage(user, image.get)
          Redirect(routes.Home.feed()).flashing("info" -> "Image posted")

        } else
          Redirect(routes.Home.feed()).flashing("alert" -> "You cannot use that image")
  }

  def feed = Account.AuthenticatedAction {
    implicit request =>
      implicit user =>
        val feed = FeedTools.getFeed(user)
        Ok(views.html.users.feed(user, feed))
  }

  def favorites = Account.AuthenticatedAction {
    implicit request =>
      implicit user =>
        val favorites = user.getFavorites
        Ok // TODO: Create view
  }
}
