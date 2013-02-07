package controllers

import play.api.mvc.{Action, Controller}
import tools.{Hasher, ImageUploader, FeedTools}
import models.{Network, Image, ActivityStream}
import anorm.NotAssigned
import java.util.Date
import tools.social.UserActions

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

  def addImage() = Account.AuthenticatedAction {
    implicit request =>
      implicit user =>

      // Handle the image upload
        val file = request.body.asMultipartFormData.get.file("image").get
        val name = request.body.asMultipartFormData.get.dataParts("name")(0)
        val image = ImageUploader.uploadPicture(file, name)

        // Create the update
        UserActions.userPostsImage(user, image)
        Ok(image.uri) // TODO: Redirect with message
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
