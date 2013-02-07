package controllers

import play.api.mvc._
import models._
import anorm.NotAssigned
import tools.{ImageUploader, Hasher}
import java.util.Date
import play.api.libs.json.Json
import scala.Some
import scala.Some
import scala.Some
import tools.social.UserActions

/**
 * The Account controller is responsible for all actions relating to the account not including social network functions.
 * These include signing up, logging in, and updating the account.
 */
object Account extends Controller {
  def signup = Action {
    implicit request =>
      Ok(views.html.account.signup())
  }

  def signupUser = Action {
    implicit request =>
    // Get the configuration
      val availableFields = ViktyoConfiguration.findByName("signup.user.availableFields").get.data.right.get.zip(
        ViktyoConfiguration.findByName("signup.user.availableFieldsTypes").get.data.right.get
      )
      val requiredFields = ViktyoConfiguration.findByName("signup.user.requiredFields").get.data.right.get

      Ok(views.html.account.signupUser(availableFields, requiredFields))
  }

  def signupOrganization = Action {
    implicit request =>
    // Get the configuration
      val availableFields = ViktyoConfiguration.findByName("signup.organization.availableFields").get.data.right.get.zip(
        ViktyoConfiguration.findByName("signup.organization.availableFieldsTypes").get.data.right.get
      )
      val requiredFields = ViktyoConfiguration.findByName("signup.organization.requiredFields").get.data.right.get

      Ok(views.html.account.signupOrganization(availableFields, requiredFields))
  }

  def checkUsername = Action(parse.urlFormEncoded) {
    implicit request =>
      val username = request.body("username")(0)
      val available = !User.findByUsername(username).isDefined
      Ok(Json.obj("available" -> available))
  }

  def createUserAccount = Action(parse.urlFormEncoded) {
    implicit request =>
      val username = request.body("username")(0)
      val fullname = request.body("full_name")(0)
      val password = request.body("password")(0)

      // Check that the username isn't already taken
      val usernameTaken = User.checkUsername(username)
      if (!usernameTaken) {

        // Create the user
        var user = User(NotAssigned, fullname, username, Hasher.sha256Base64(password))
          .setProperty("accountType", "user")

        // Add the additional fields
        val availableFields = ViktyoConfiguration.findByName("signup.user.availableFields").get.data.right.get
        for (field <- availableFields) {
          val name = field.toLowerCase.replaceAll(" ", "_")
          val value = request.body(name)(0)
          if (!value.isEmpty)
            user = user.setProperty(name, value)
        }

        // Save the user
        user.save
        Redirect(routes.Home.feed()).withSession("username" -> username)
          .flashing("success" -> ("Welcome to VIKTYO " + username + "!"))

      } else // Username already taken
        Redirect(routes.Account.signupUser()).flashing("alert" -> "That username is already taken.")
  }

  def createOrganizationAccount = Action(parse.urlFormEncoded) {
    implicit request =>
      val username = request.body("username")(0)
      val orgName = request.body("organization_name")(0)
      val password = request.body("password")(0)

      // Check that the username isn't already taken
      val usernameTaken = User.checkUsername(username)
      if (!usernameTaken) {

        var user = User(NotAssigned, orgName, username, Hasher.sha256Base64(password))
          .setProperty("accountType", "organization")

        // Add the additional fields
        val availableFields = ViktyoConfiguration.findByName("signup.organization.availableFields").get.data.right.get
        for (field <- availableFields) {
          val name = field.toLowerCase.replaceAll(" ", "_")
          val value = request.body(name)(0)
          if (!value.isEmpty)
            user = user.setProperty(name, value)
        }

        // Save the user
        user.save
        Redirect(routes.Home.feed()).withSession("username" -> username)
          .flashing("success" -> ("Welcome to VIKTYO " + username + "!"))

      } else // Username already taken
        Redirect(routes.Account.signupOrganization()).flashing("alert" -> "That username is already taken.")
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
        Redirect(routes.Application.index()).flashing("alert" -> "Bad username/password.")
  }

  def logout = Action {
    implicit request =>
      Redirect(routes.Application.index()).withNewSession
  }

  def forgotPasswordPage = Action {
    implicit request =>
      Ok(views.html.account.forgotPassword())
  }

  def forgotPassword = Action(parse.urlFormEncoded) {
    implicit request =>
      val username = request.body("username")(0)

      // Check that the user exists
      var user = User.findByUsername(username)
      if (user.isDefined) {
        val code = Hasher.md5Hex(new Date().getTime + username)
        user = Some(user.get.setProperty("emailResetCode", code).save)
        tools.Emailer.sendPasswordRecoveryEmail(user.get)
        Redirect(routes.Application.index()).flashing("success" -> "An email was sent. Please check your inbox.")

      } else // User doesn't exist
        Redirect(routes.Account.forgotPasswordPage()).flashing("alert" -> "There is no user by that username.")
  }

  // Password reset page
  def passwordResetPage(code: String) = Action {
    implicit request =>

      implicit val user = User.findByProperty("emailResetCode", code)
      if (user.isDefined) {
        Ok(views.html.account.passwordReset(code))

      } else // Bad email code
        Redirect(routes.Application.index()).flashing("alert" -> "That's a bad email reset code.")
  }

  // Password reset action
  def passwordReset(code: String) = Action(parse.urlFormEncoded) {
    implicit request =>
      val password = request.body("password")(0)

      // Get the user by the code
      implicit val user = User.findByProperty("emailResetCode", code)
      if (user.isDefined) {
        user.get.removeProperty("emailResetCode").setPassword(Hasher.sha256Base64(password)).save
        Redirect(routes.Application.index()).flashing("success" -> "Your password was reset!")

      } else // Bad email code
        Redirect(routes.Application.index()).flashing("alert" -> "That's a bad email reset code.")
  }

  def profile = AuthenticatedAction {
    implicit request =>
      implicit user =>
        Ok(views.html.account.profile())
  }

  def settings = AuthenticatedAction {
    implicit request =>
      implicit user =>
        Ok(views.html.account.settings())
  }

  def changePassword = AuthenticatedAction {
    implicit request =>
      implicit user =>
        val password = request.body.asFormUrlEncoded.get("password")(0)
        user.setPassword(Hasher.sha256Base64(password)).save
        Redirect(routes.Account.settings()).flashing("success" -> "Your password was changed.")
  }

  def setProperty() = AuthenticatedAction {
    implicit request =>
      implicit user =>
        val attribute = request.body.asFormUrlEncoded.get("attribute")(0)
        val value = request.body.asFormUrlEncoded.get("value")(0)

        // Check if we're setting one of the user attributes
        if (attribute == "fullname")
          user.setFullname(value).save
        else if (attribute == "username")
          user.setUsername(value).save
        else
          user.setProperty(attribute, value).save
        Ok // TODO: Redirect with message
  }

  def removeProperty() = AuthenticatedAction {
    implicit request =>
      implicit user =>
        val attribute = request.body.asFormUrlEncoded.get("attribute")(0)
        user.removeProperty(attribute).save
        Ok // TODO: Redirect with message
  }

  def profilePicture = AuthenticatedAction {
    implicit request =>
      implicit user =>

      // Handle the image upload
        val file = request.body.asMultipartFormData.get.file("image").get
        val name = "Profile Picture"
        val image = ImageUploader.uploadProfilePicture(file, name)

        // Set the profile picture
        UserActions.userSetProfilePicture(user, image)
        Redirect(routes.Account.settings()).flashing("success" -> "Your profile picture was changed.")
  }

  def notifications = AuthenticatedAction {
    implicit request =>
      implicit user =>
        val notifications = ViktyoNotification.listByUser(user.id.get)
        Ok // TODO: Redirect with message
  }

  def readNotification = AuthenticatedAction {
    implicit request =>
      implicit user =>

      // Check that the notification he is reading is real and his
        val notification = ViktyoNotification.findById(request.body.asFormUrlEncoded.get("notification")(0).toLong)
        if (notification.isDefined && notification.get.user == user.id.get) {
          notification.get.markRead.save
          Ok // TODO: Redirect with message

        } else
          Ok // TODO: Redirect with message
  }

  // Authentication helpers
  def getCurrentUser()(implicit request: RequestHeader): Option[User] = {
    if (request.session.get("username").isDefined)
      User.findByUsername(request.session("username"))
    else
      None
  }

  def AuthenticatedAction(f: Request[AnyContent] => (User => Result)) = {
    Action {
      implicit request =>
        val user = getCurrentUser
        if (user.isDefined) {
          f(request)(user.get)
        } else // User not logged in
          Redirect(routes.Application.index()).flashing("alert" -> "You are not logged in")
    }
  }
}
