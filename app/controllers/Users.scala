package controllers

import play.api.mvc._
import models.{ViktyoNotification, ActivityStream, User}
import tools.FeedTools
import tools.social.UserActions

/**
 * Created with IntelliJ IDEA.
 * User: Josh
 * Date: 1/25/13
 * Time: 7:32 AM
 * To change this template use File | Settings | File Templates.
 */
object Users extends Controller {

  def AuthenticatedUserAction(id: Long)(f: Request[AnyContent] => (User => (User => Result))) = {
    Account.AuthenticatedAction {
      request =>
        user =>
          val otherUser = User.findById(id)
          if (otherUser.isDefined)
            f(request)(user)(otherUser.get)
          else
            Redirect(routes.Postings.browseList()).flashing("alert" -> "That user doesn't exist.")
    }
  }

  def view(id: Long) = AuthenticatedUserAction(id) {
    implicit request =>
      implicit user =>
        otherUser =>

          if (otherUser == user)
            Redirect(routes.Home.feed())
          else {
            val feed = FeedTools.getFeed(otherUser)
            Ok(views.html.users.feed(otherUser, feed))
          }
  }

  def follow(id: Long) = AuthenticatedUserAction(id) {
    implicit request =>
      implicit user =>
        otherUser =>
          UserActions.userFollows(user, otherUser)
          Redirect(routes.Users.view(id)).flashing("info" -> ("You are now following " + otherUser.fullname))
  }

  def unfollow(id: Long) = AuthenticatedUserAction(id) {
    implicit request =>
      implicit user =>
        otherUser =>
          UserActions.userUnfollows(user, otherUser)
          Redirect(routes.Users.view(id)).flashing("info" -> ("You are no longer following " + otherUser.fullname))
  }

  def message(id: Long) = AuthenticatedUserAction(id) {
    implicit request =>
      implicit user =>
        otherUser =>
          Ok // TODO: Create view
  }

  def sendMessage(id: Long) = AuthenticatedUserAction(id) {
    implicit request =>
      implicit user =>
        otherUser =>
          // Create the message
          val message = request.body.asFormUrlEncoded.get("message")(0)
          ViktyoNotification.createMessage(user, otherUser, message).save
          Redirect(routes.Users.view(id)).flashing("info" -> "Message sent")
  }

}
