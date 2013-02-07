package controllers

import play.api.mvc._
import models.{ActivityStream, User, Network}
import tools.ImageUploader
import tools.social.NetworkActions

/**
 * Controller for network actions
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
            Redirect(routes.Networks.networkFeed(id)).flashing("info" -> "Status updated")

          } else // Not part of network
            Redirect(routes.Home.feed()).flashing("alert" -> "You are not part of that network.")
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
            Redirect(routes.Networks.networkFeed(id)).flashing("info" -> "Image added")

          } else // Not part of network
            Redirect(routes.Home.feed()).flashing("alert" -> "You are not part of that network.")
  }

  def removeActivityStream(networkId: Long, id: Long) = AuthenticatedNetworkAction(networkId) {
    implicit request =>
      implicit user =>
        network =>

          val activityStream = ActivityStream.findById(id)
          if(activityStream.isDefined) {
            activityStream.get.delete()
            Redirect(routes.Networks.networkFeed(networkId)).flashing("info" -> "Item removed")
          } else
            Redirect(routes.Networks.networkFeed(networkId)).flashing("alert" -> "You cannot remove that item")
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
          // TODO: Get network feed
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
