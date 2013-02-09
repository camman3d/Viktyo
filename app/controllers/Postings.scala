package controllers

import play.api.mvc._
import models._
import tools.ImageUploader
import tools.social.PostingActions
import play.api.libs.json.{Json, JsArray}
import anorm.NotAssigned
import java.util.Date

object Postings extends Controller {

  def AuthenticatedPostingAction(id: Long)(f: Request[AnyContent] => (User => (Posting => Result))) = {
    Account.AuthenticatedAction {
      request =>
        user =>
          val posting = Posting.findById(id)
          if (posting.isDefined)
            f(request)(user)(posting.get)
          else
            Redirect(routes.Postings.browseList()).flashing("alert" -> "That listing doesn't exist.")
    }
  }

  def addComment(id: Long) = AuthenticatedPostingAction(id) {
    implicit request =>
      implicit user =>
        posting =>

          // Create the comment
          val comment = request.body.asFormUrlEncoded.get("comment")(0)
          PostingActions.userComments(user, comment, posting)
          Redirect(routes.Postings.view(id)).flashing("info" -> "Comment added.")
  }

  def addImage(id: Long) = AuthenticatedPostingAction(id) {
    implicit request =>
      implicit user =>
        posting =>

          // Handle the image upload
          val file = request.body.asMultipartFormData.get.file("image").get
          val name = request.body.asMultipartFormData.get.dataParts("name")(0)
          val image = ImageUploader.uploadPicture(file, name)

          // Create the update
          PostingActions.userPostsImage(user, image, posting)
          Redirect(routes.Postings.view(id)).flashing("info" -> "Image added.")
  }

  def browse(view: Symbol) = Account.AuthenticatedAction {
    implicit request =>
      implicit user =>

        // Get all the postings
        val postings = Posting.list
        val postingsJson = JsArray(postings.map(_.toJson)).toString()
        if (view == 'list)
          Ok(views.html.postings.browseList(postingsJson))
        else
          Ok(views.html.postings.browseMap(postingsJson))
  }

  def browseList = browse('list)

  def browseMap = browse('map)

  def deleteActivityStream(postId: Long, id: Long) = AuthenticatedPostingAction(postId) {
    implicit request =>
      implicit user =>
        posting =>

          // Check that the comment is real and the user made the comment
          val comment = ActivityStream.findById(id)
          if (comment.isDefined && comment.get.actor == user) {
            comment.get.delete()
            Redirect(routes.Postings.view(postId)).flashing("info" -> "Item removed")
          } else
            Redirect(routes.Postings.view(postId)).flashing("alert" -> "You cannot remove that item")
  }

  def favorite(id: Long) = AuthenticatedPostingAction(id) {
    implicit request =>
      implicit user =>
        posting =>

          // Favorite it
          PostingActions.userFavorites(user, posting)
          Redirect(routes.Postings.view(id)).flashing("info" -> "This listing is now part of your favorites.")
  }

  def unfavorite(id: Long) = AuthenticatedPostingAction(id) {
    implicit request =>
      implicit user =>
        posting =>

          // Unfavorite it
          PostingActions.userUnfavorites(user, posting)
          Redirect(routes.Postings.view(id)).flashing("info" -> "This listing is no longer part of your favorites.")
  }

  def follow(id: Long) = AuthenticatedPostingAction(id) {
    implicit request =>
      implicit user =>
        posting =>

          // Follow it
          PostingActions.userFollows(user, posting)
          Redirect(routes.Postings.view(id)).flashing("info" -> "You are now following this listing.")
  }

  def unfollow(id: Long) = AuthenticatedPostingAction(id) {
    implicit request =>
      implicit user =>
        posting =>

          // Unfollow it
          PostingActions.userUnfollows(user, posting)
          Redirect(routes.Postings.view(id)).flashing("info" -> "You are no longer following this listing.")
  }

  def view(id: Long) = AuthenticatedPostingAction(id) {
    implicit request =>
      implicit user =>
        posting =>

          // Get the images
          val images = ActivityStream.listByTarget(posting.objId).filter(_.verb == "imagePost").map(a =>
            (a, Image.findByObjId(a.obj).get)
          )

          // Get the comments
          val comments = ActivityStream.listByTarget(posting.objId).filter(_.verb == "comment").map(a =>
            (a, Text.findByObjId(a.obj).get)
          )

          // Get those who favorited and follow
          val favorites = posting.getFavorites
          val followers = posting.getFollowers

          // Increment the views
          val updatedPosting = posting.incrementViews.save
          Ok(views.html.postings.view(updatedPosting, favorites, followers, images, comments))
  }

  def create = Account.AuthenticatedAction {
    implicit request =>
      implicit user =>

        // Make sure the user is an organization or admin
        val accountType = user.getProperty("accountType")
        if (accountType == "organization" || accountType == "admin") {
          val posting = Posting.createFromRequest.save
          PostingActions.orgCreates(user, posting)
          Redirect(routes.Postings.view(posting.id.get)).flashing("success" -> "Listing created")
        } else
          Redirect(routes.Application.index()).flashing("alert" -> "You are not authorized to create listings.")
  }

  def setPanoramio(id: Long) = AuthenticatedPostingAction(id) {
    implicit request =>
      implicit user =>
        posting =>

      // Make sure the user is an organization or admin
        val accountType = user.getProperty("accountType")
        if (accountType == "organization" || accountType == "admin") {

          // Make sure the organization owns the posting
          if (posting.poster == user) {
            val panoramio = request.body.asFormUrlEncoded.get("panoramio")(0)
            posting.setProperty("panoramio", panoramio)
            val uri = (Json.parse(panoramio) \ "photo_file_url").as[String]
            val image = Image(NotAssigned, posting.name, uri).setProperty("owner", user.id.get.toString)
              .setProperty("added", new Date().getTime.toString).save
            PostingActions.orgUpdatesCover(user, posting, image)

            Redirect(routes.Postings.view(posting.id.get)).flashing("info" -> "Listing cover image updated")
          } else
            Redirect(routes.Postings.view(id)).flashing("alert" -> "You are not authorized to edit this listing.")
        } else
          Redirect(routes.Postings.view(id)).flashing("alert" -> "You are not authorized to edit listings.")
  }

  def setCover(id: Long) = AuthenticatedPostingAction(id) {
    implicit request =>
      implicit user =>
        posting =>

        // Make sure the user is an organization or admin
          val accountType = user.getProperty("accountType")
          if (accountType == "organization" || accountType == "admin") {

            // Make sure the organization owns the posting
            if (posting.poster == user) {

              // Handle the image upload
              val file = request.body.asMultipartFormData.get.file("image").get
              val name = request.body.asMultipartFormData.get.dataParts("name")(0)
              val image = ImageUploader.uploadPicture(file, name)
              PostingActions.orgUpdatesCover(user, posting, image)

              Redirect(routes.Postings.view(posting.id.get)).flashing("info" -> "Listing cover image updated")
            } else
              Redirect(routes.Postings.view(id)).flashing("alert" -> "You are not authorized to edit this listing.")
          } else
            Redirect(routes.Postings.view(id)).flashing("alert" -> "You are not authorized to edit listings.")
  }
}
