package controllers

import play.api.mvc._
import models._
import tools.{ImageUploader, Hasher}
import java.util.Date
import anorm.NotAssigned
import play.api.libs.json.{JsArray, Json}
import tools.social.PostingActions
import play.api.libs.json.JsArray

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
          PostingActions.userComments(user.get, comment, posting.get)

          Redirect(routes.Postings.view(id)).flashing("success" -> "Comment added.")

        } else // Network doesn't exist
          Ok // TODO: Redirect with message
      } else // User not logged in
        Redirect(routes.Application.index()).flashing("alert" -> "You are not logged in")
  }

  def addImage(id: Long) = Action(parse.multipartFormData) {
    implicit request =>
    // Check that the user is logged in
      implicit val user = Account.getCurrentUser
      if (user.isDefined) {

        // Check that the posting is real
        val posting = Posting.findById(id)
        if (posting.isDefined) {

          // Handle the image upload
          val file = request.body.file("image").get
          val name = request.body.dataParts("name")(0)
          val image = ImageUploader.uploadPicture(file, name)

          // Create the update
          PostingActions.userPostsImage(user.get, image, posting.get)
          Ok(image.uri) // TODO: Redirect with message

        } else // Network doesn't exist
          Ok // TODO: Redirect with message
      } else // User not logged in
        Redirect(routes.Application.index()).flashing("alert" -> "You are not logged in")
  }

  def browse(view: Symbol) = Action {
    implicit request =>

    // Check that the user is logged in
      implicit val user = Account.getCurrentUser
      if (user.isDefined) {
        val postings = Posting.list
        val postingsJson = JsArray(postings.map(_.toJson)).toString()
        if (view == 'list)
          Ok(views.html.postings.browseList(postingsJson))
        else
          Ok(views.html.postings.browseMap(postingsJson))

      } else // User not logged in
        Redirect(routes.Application.index()).flashing("alert" -> "You are not logged in")
  }

  def browseList = browse('list)

  def browseMap = browse('map)

  def deleteActivityStream(postId: Long, id: Long) = Action(parse.urlFormEncoded) {
    implicit request =>

    // Check that the user is logged in
      val user = Account.getCurrentUser
      if (user.isDefined) {

        // Check that the posting is real
        val posting = Posting.findById(postId)
        if (posting.isDefined) {

          // Check that the comment is real and the user made the comment
          val comment = ActivityStream.findById(id)
          if (comment.isDefined && comment.get.actor == user.get) {
            comment.get.delete()
            Ok // TODO: Redirect with message

          } else // Can't delete
            Ok // TODO: Redirect with message
        } else // Network doesn't exist
          Ok // TODO: Redirect with message
      } else // User not logged in
        Redirect(routes.Application.index()).flashing("alert" -> "You are not logged in")
  }

  def favorite(id: Long) = Action {
    implicit request =>

    // Check that the user is logged in
      val user = Account.getCurrentUser
      if (user.isDefined) {

        // Check that the posting is real
        val posting = Posting.findById(id)
        if (posting.isDefined) {

          // Favorite it
          user.get.addFavorite(posting.get).save
          // TODO: Add to posting favoriters

          // Create the update
          ActivityStream.createFavorite(user.get, posting.get).save
          Redirect(routes.Postings.view(id)).flashing("success" -> "This listing is now part of your favorites.")

        } else // Network doesn't exist
          Ok // TODO: Redirect with message
      } else // User not logged in
        Redirect(routes.Application.index()).flashing("alert" -> "You are not logged in")
  }

  def unfavorite(id: Long) = Action {
    implicit request =>

    // Check that the user is logged in
      val user = Account.getCurrentUser
      if (user.isDefined) {

        // Check that the posting is real
        val posting = Posting.findById(id)
        if (posting.isDefined) {

          // Unfavorite it
          user.get.removeFavorite(posting.get).save

          // Delete from activity stream
          ActivityStream.find(user.get, "favorite", posting.get.objId, posting.get.objId).get.delete()
          Redirect(routes.Postings.view(id)).flashing("success" -> "This listing is no longer part of your favorites.")

        } else // Network doesn't exist
          Ok // TODO: Redirect with message
      } else // User not logged in
        Redirect(routes.Application.index()).flashing("alert" -> "You are not logged in")
  }

  def view(id: Long) = Action {
    implicit request =>

    // Check that the user is logged in
      implicit val user = Account.getCurrentUser
      if (user.isDefined) {

        // Make sure the posting exists
        val posting = Posting.findById(id)
        if (posting.isDefined) {

          // Get the images
          val images = ActivityStream.listByTarget(posting.get.objId).filter(_.verb == "imagePost").map(a =>
            (a, Image.findByObjId(a.obj).get)
          )

          // Get the comments
          val comments = ActivityStream.listByTarget(posting.get.objId).filter(_.verb == "comment").map(a =>
            (a, Text.findByObjId(a.obj).get)
          )

          // Increment the views
          val updatedPosting = posting.get.incrementViews.save
          Ok(views.html.postings.view(updatedPosting, images, comments))

        } else // Posting doesn't exist
          Redirect(routes.Application.index()).flashing("error" -> "The listing doesn't exist")
      } else // User not logged in
        Redirect(routes.Application.index()).flashing("alert" -> "You are not logged in")
  }
}
