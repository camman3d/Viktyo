package controllers

import play.api.mvc.{Action, Controller}
import tools.{Hasher, ImageUploader, EdgeRank}
import models.{Network, Image, ActivityStream}
import anorm.NotAssigned
import java.util.Date

/**
 * Created with IntelliJ IDEA.
 * User: Josh
 * Date: 1/24/13
 * Time: 11:54 AM
 * To change this template use File | Settings | File Templates.
 */
object Home extends Controller {

  def addStatusUpdate() = Action(parse.urlFormEncoded) {
    implicit request =>

      // Check that the user is logged in
      implicit val user = Account.getCurrentUser
      if (user.isDefined) {

        // Create the status update
        val statusUpdate = request.body("statusUpdate")(0)
        ActivityStream.createStatusUpdate(user.get, statusUpdate, user.get.objId).save
        Ok // TODO: Redirect with message

      } else // User not logged in
        Redirect(routes.Application.index()).flashing("alert" -> "You are not logged in")
  }

  def addImage() = Action(parse.multipartFormData) { implicit request =>
    // Check that the user is logged in
    implicit val user = Account.getCurrentUser
    if (user.isDefined) {

      // Handle the image upload
      val file = request.body.file("image").get
      val id = user.get.username + "-" + Hasher.md5Hex(file.filename + new Date().getTime)
      val uri = ImageUploader.upload(file, id, file.contentType.get)
      val name = request.body.dataParts("name")(0)
      val image = Image(NotAssigned, name, uri).setProperty("filename", file.filename).save

      // Create the update
      ActivityStream.createImagePost(user.get, image, user.get.objId).save
      Ok(uri) // TODO: Redirect with message

    } else // User not logged in
      Redirect(routes.Application.index()).flashing("alert" -> "You are not logged in")
  }

  def feed = Action {
    implicit request =>

    // Check that the user is logged in
      implicit val user = Account.getCurrentUser
      if (user.isDefined) {
        val feed = EdgeRank.getFeed(user.get)
        Ok(views.html.account.feed(feed))

      } else // User not logged in
        Redirect(routes.Application.index()).flashing("alert" -> "You are not logged in")
  }
}
