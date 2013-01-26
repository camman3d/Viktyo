package controllers

import play.api.mvc.{RequestHeader, Action, Controller}
import models.{ViktyoNotification, User}
import anorm.NotAssigned
import tools.Hasher
import java.util.Date

/**
 * The Account controller is responsible for all actions relating to the account not including social network functions.
 * These include signing up, logging in, and updating the account.
 */
object Account extends Controller {
  def signup = TODO

  def signupUser = TODO

  def signupOrganization = TODO

  def createUserAccount = Action(parse.urlFormEncoded) {
    implicit request =>
      val username = request.body("username")(0)
      val fullname = request.body("fullname")(0)
      val email = request.body("email")(0)
      val password = request.body("password")(0)

      // Check that the username isn't already taken
      val usernameTaken = User.checkUsername(username)
      if (!usernameTaken) {

        val user = User(NotAssigned, fullname, username, Hasher.sha256Base64(password))
          .setProperty("accountType", "user").setProperty("email", email).save
        Ok // TODO: Redirect with message and session

      } else // Username already taken
        Ok // TODO: Redirect with message
  }

  def createOrganizationAccount = Action(parse.urlFormEncoded) {
    implicit request =>
      val username = request.body("username")(0)
      val orgName = request.body("orgname")(0)
      val email = request.body("email")(0)
      val password = request.body("password")(0)

      // Check that the username isn't already taken
      val usernameTaken = User.checkUsername(username)
      if (!usernameTaken) {

        val user = User(NotAssigned, orgName, username, Hasher.sha256Base64(password))
          .setProperty("accountType", "organization").setProperty("email", email).save
        Ok // TODO: Redirect with message and session

      } else // Username already taken
        Ok // TODO: Redirect with message
  }

  def login = Action(parse.urlFormEncoded) {
    implicit request =>
      val username = request.body("username")(0)
      val password = Hasher.sha256Base64(request.body("password")(0))

      // Check that the username/password pair is correct
      val user = User.authenticate(username, password)
      if (user.isDefined) {
        Redirect(routes.Home.feed()).withSession("username" -> username)

      } else // Bad credentials
        Ok // TODO: Redirect with message
  }

  def logout = Action {
    implicit request =>
      Ok // TODO: Redirect with new session
  }

  // Ajax call
  def forgotPassword = Action(parse.urlFormEncoded) {
    implicit request =>
      val username = request.body("username")(0)

      // Check that the user exists
      val user = User.findByUsername(username)
      if (user.isDefined) {
        val code = Hasher.md5Hex(new Date().getTime + username)
        user.get.setProperty("emailResetCode", code).save

        Ok // TODO: Respond with success message

      } else // User doesn't exist
        Ok // TODO: Respond with error message
  }

  // Password reset page
  def passwordResetPage(code: String) = TODO

  // Password reset action
  def passwordReset(code: String) = Action(parse.urlFormEncoded) {
    implicit request =>
      val password = request.body("password")(0)

      // Get the user by the code
      val user = User.findByProperty("emailResetCode", code)
      if (user.isDefined) {
        user.get.removeProperty("emailResetCode").setPassword(Hasher.sha256Base64(password)).save
        Ok

      } else // Bad email code
        Ok // TODO: Redirect with message
  }

  def profile = TODO

  def changePassword = Action(parse.urlFormEncoded) {
    implicit request =>

    // Check that the user is logged in
      val user = getCurrentUser
      if (user.isDefined) {
        val password = request.body("password")(0)
        user.get.setPassword(Hasher.sha256Base64(password)).save
        Ok // TODO: Redirect with message
      } else // Not logged in
        Redirect(routes.Application.index()).flashing("alert" -> "You are not logged in")
  }

  def setProperty() = Action(parse.urlFormEncoded) {
    implicit request =>

    // Check that the user is logged in
      val user = getCurrentUser
      if (user.isDefined) {
        val attribute = request.body("attribute")(0)
        val value = request.body("value")(0)

        // Check if we're setting one of the user attributes
        if (attribute == "fullname")
          user.get.setFullname(value).save
        else if (attribute == "username")
          user.get.setUsername(value).save
        else
          user.get.setProperty(attribute, value).save
        Ok // TODO: Redirect with message

      } else // Not logged in
        Redirect(routes.Application.index()).flashing("alert" -> "You are not logged in")
  }

  def removeProperty() = Action(parse.urlFormEncoded) {
    implicit request =>

    // Check that the user is logged in
      val user = getCurrentUser
      if (user.isDefined) {
        val attribute = request.body("attribute")(0)
        user.get.removeProperty(attribute).save
        Ok // TODO: Redirect with message

      } else // Not logged in
        Redirect(routes.Application.index()).flashing("alert" -> "You are not logged in")
  }

  // Non action functions
  def getCurrentUser()(implicit request: RequestHeader): Option[User] = {
    if (request.session.get("username").isDefined)
      User.findByUsername(request.session("username"))
    else
      None
  }

  def notifications = Action { implicit request =>
    // Check that the user is logged in
    val user = getCurrentUser
    if (user.isDefined) {

      val notifications = ViktyoNotification.listByUser(user.get.id.get)
      Ok // TODO: Redirect with message

    } else // Not logged in
      Redirect(routes.Application.index()).flashing("alert" -> "You are not logged in")
  }

  def readNotification = Action(parse.urlFormEncoded) { implicit request =>

    // Check that the user is logged in
    val user = getCurrentUser
    if (user.isDefined) {

      // Check that the notification he is reading is real and his
      val notification = ViktyoNotification.findById(request.body("notification")(0).toLong)
      if(notification.isDefined && notification.get.user == user.get) {
        notification.get.markRead.save
        Ok // TODO: Redirect with message

      } else
        Ok // TODO: Redirect with message
    } else // Not logged in
      Redirect(routes.Application.index()).flashing("alert" -> "You are not logged in")
  }
}
