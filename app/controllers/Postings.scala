package controllers

import play.api.mvc.{Action, Controller}
import models._
import tools.{ImageUploader, Hasher}
import java.util.Date
import anorm.NotAssigned

/**
 * Created with IntelliJ IDEA.
 * User: Josh
 * Date: 1/24/13
 * Time: 3:12 PM
 * To change this template use File | Settings | File Templates.
 */
object Postings extends Controller {

  def addComment(id: Long) = Action(parse.urlFormEncoded) {
    implicit request =>

    // Check that the user is logged in
      val user = Account.getCurrentUser
      if (user.isDefined) {

        // Check that the posting is real
        val posting = Posting.findById(id)
        if (posting.isDefined) {

            // Create the comment
            val comment = request.body("comment")(0)
            ActivityStream.createComment(user.get, comment, posting.get.objId).save
            Ok // TODO: Redirect with message

        } else // Network doesn't exist
          Ok // TODO: Redirect with message
      } else // User not logged in
        Redirect(routes.Application.index()).flashing("alert" -> "You are not logged in")
  }

  def addImage(id: Long) = Action(parse.multipartFormData) {
    implicit request =>
    // Check that the user is logged in
      val user = Account.getCurrentUser
      if (user.isDefined) {

        // Check that the posting is real
        val posting = Posting.findById(id)
        if (posting.isDefined) {

            // Handle the image upload
            val file = request.body.file("image").get
            val id = user.get.username + "-" + Hasher.md5Hex(file.filename + new Date().getTime)
            val uri = ImageUploader.upload(file, id, file.contentType.get)
            val name = request.body.dataParts("name")(0)
            val image = Image(NotAssigned, name, uri).setProperty("filename", file.filename).save

            // Create the update
            ActivityStream.createImagePost(user.get, image, posting.get.objId).save
            Ok(uri) // TODO: Redirect with message

        } else // Network doesn't exist
          Ok // TODO: Redirect with message
      } else // User not logged in
        Redirect(routes.Application.index()).flashing("alert" -> "You are not logged in")
  }

  def browse(view: Symbol) = Action { implicit request =>

  // Check that the user is logged in
    val user = Account.getCurrentUser
    if (user.isDefined) {
      val postings = Posting.list
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

      // Make sure the posting exists
      val posting = Posting.findById(id)
      if (posting.isDefined) {
        Ok // TODO: Create view

      } else // Posting doesn't exist
        Ok // TODO: Redirect with message
    } else // User not logged in
      Redirect(routes.Application.index()).flashing("alert" -> "You are not logged in")
  }
}
