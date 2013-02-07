package controllers

import play.api.mvc._
import models.{User, Image, ActivityStream, Network}
import tools.{ImageUploader, Hasher}
import java.util.Date
import anorm.NotAssigned
import tools.social.NetworkActions

/**
 * Created with IntelliJ IDEA.
 * User: Josh
 * Date: 1/24/13
 * Time: 2:44 PM
 * To change this template use File | Settings | File Templates.
 */
object Networks extends Controller {

  def AuthenticatedNetworkAction(id: Long)(f: Request[AnyContent] => (User => (Network => Result))) = {
    Account.AuthenticatedAction {
      implicit request =>
        user =>
          val network = Network.findById(id)
          if (network.isDefined)
            f(request)(user)(network.get)
          else
            Redirect(routes.Networks.browse()).flashing("alert" -> "That network doesn't exist")
    }
  }

  def addStatusUpdate(id: Long) = AuthenticatedNetworkAction(id) {
    implicit request =>
      implicit user =>
        network =>

        // Make sure the user is part of the network
          if (user.hasNetwork(network)) {

            // Create the status update
            val statusUpdate = request.body.asFormUrlEncoded.get("statusUpdate")(0)
            NetworkActions.userPostsStatusUpdate(user, statusUpdate, network)
            Ok // TODO: Redirect with message

          } else // Not part of network
            Ok // TODO: Redirect with message
  }

  def addImage(id: Long) = AuthenticatedNetworkAction(id) {
    implicit request =>
      implicit user =>
        network =>

          // Make sure the user is part of the network
          if (user.hasNetwork(network)) {

            // Handle the image upload
            val file = request.body.asMultipartFormData.get.file("image").get
            val name = request.body.asMultipartFormData.get.dataParts("name")(0)
            val image = ImageUploader.uploadPicture(file, name)

            // Create the update
            NetworkActions.userPostsImage(user, image, network)
            Ok(image.uri) // TODO: Redirect with message

          } else // Not part of network
            Ok // TODO: Redirect with message
  }

  def browse = Account.AuthenticatedAction {
    implicit request =>
      implicit user =>
        val page: Int = request.queryString.get("page").getOrElse(Seq("0"))(0).toInt
        val networks = Network.list(page)
        Ok // TODO: Create view
  }

  def networkFeed(id: Long) = AuthenticatedNetworkAction(id) {
    implicit request =>
      implicit user =>
        network =>
          Ok // TODO: Create view
  }

  def joinNetwork(id: Long) = AuthenticatedNetworkAction(id) {
    implicit request =>
      implicit user =>
        network =>
          NetworkActions.userJoins(user, network)
          Ok // TODO: Redirect with message
  }

  def leaveNetwork(id: Long) = AuthenticatedNetworkAction(id) {
    implicit request =>
      implicit user =>
        network =>
          NetworkActions.userLeaves(user, network)
          Ok // TODO: Redirect with message
  }

  def viewMembers(id: Long) = AuthenticatedNetworkAction(id) {
    implicit request =>
      implicit user =>
        network =>
          // Check that the user is an organization
          if (user.getProperty("accountType") == "organization") {
            val members = network.getMembers
            Ok // TODO: Redirect with message
          } else // User not logged in
            Redirect(routes.Application.index()).flashing("alert" -> "You are not authorized to view the members.")
  }

}
