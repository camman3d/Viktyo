package controllers

import play.api.mvc.{Action, Controller}
import models.{ViktyoNotification, ActivityStream, User}

/**
 * Created with IntelliJ IDEA.
 * User: Josh
 * Date: 1/25/13
 * Time: 7:32 AM
 * To change this template use File | Settings | File Templates.
 */
object Users extends Controller {

  def view(id: Long) = Action {
    implicit request =>

    // Check that the user is logged in
      val user = Account.getCurrentUser
      if (user.isDefined) {

        // Make sure the other user exists
        val otherUser = User.findById(id)
        if (otherUser.isDefined) {
          Ok // TODO: Create view

        } else // Posting doesn't exist
          Ok // TODO: Redirect with message
      } else // User not logged in
        Redirect(routes.Application.index()).flashing("alert" -> "You are not logged in")
  }

  def follow(id: Long) = Action {
    implicit request =>

    // Check that the user is logged in
      val user = Account.getCurrentUser
      if (user.isDefined) {

        // Make sure the other user exists
        val otherUser = User.findById(id)
        if (otherUser.isDefined) {
          user.get.addFollowing(otherUser.get).save
          otherUser.get.addFollower(user.get).save

          // Add notification
          ViktyoNotification.createFollowing(user.get, otherUser.get).save

          // Create activity stream
          ActivityStream.createFollowUser(user.get, otherUser.get).save
          Ok // TODO: Create view

        } else // Posting doesn't exist
          Ok // TODO: Redirect with message
      } else // User not logged in
        Redirect(routes.Application.index()).flashing("alert" -> "You are not logged in")
  }

  def unfollow(id: Long) = Action {
    implicit request =>

    // Check that the user is logged in
      val user = Account.getCurrentUser
      if (user.isDefined) {

        // Make sure the other user exists
        val otherUser = User.findById(id)
        if (otherUser.isDefined) {
          user.get.removeFollowing(otherUser.get).save
          otherUser.get.removeFollower(user.get).save

          // Delete activity stream
          ActivityStream.find(user.get, "follow", otherUser.get.objId, user.get.objId).get.delete()
          Ok // TODO: Create view

        } else // Posting doesn't exist
          Ok // TODO: Redirect with message
      } else // User not logged in
        Redirect(routes.Application.index()).flashing("alert" -> "You are not logged in")
  }

  def message(id: Long) = Action {
    implicit request =>

    // Check that the user is logged in
      val user = Account.getCurrentUser
      if (user.isDefined) {

        // Make sure the other user exists
        val otherUser = User.findById(id)
        if (otherUser.isDefined) {
          Ok // TODO: Create view

        } else // Posting doesn't exist
          Ok // TODO: Redirect with message
      } else // User not logged in
        Redirect(routes.Application.index()).flashing("alert" -> "You are not logged in")
  }

  def sendMessage(id: Long) = Action(parse.urlFormEncoded) {
    implicit request =>

    // Check that the user is logged in
      val user = Account.getCurrentUser
      if (user.isDefined) {

        // Make sure the other user exists
        val otherUser = User.findById(id)
        if (otherUser.isDefined) {

          // Create the message
          val message = request.body("message")(0)
          ViktyoNotification.createMessage(user.get, otherUser.get, message).save

          // Create activity stream
          ActivityStream.createFollowUser(user.get, otherUser.get).save
          Ok // TODO: Redirect with message

        } else // Posting doesn't exist
          Ok // TODO: Redirect with message
      } else // User not logged in
        Redirect(routes.Application.index()).flashing("alert" -> "You are not logged in")
  }

}
