package controllers

import play.api.mvc.{Action, Controller}
import models.{Image, ActivityStream, Network}
import tools.{ImageUploader, Hasher}
import java.util.Date
import anorm.NotAssigned

/**
 * Created with IntelliJ IDEA.
 * User: Josh
 * Date: 1/24/13
 * Time: 2:44 PM
 * To change this template use File | Settings | File Templates.
 */
object Networks extends Controller {

  def addStatusUpdate(id: Long) = Action(parse.urlFormEncoded) {
    implicit request =>

    // Check that the user is logged in
      val user = Account.getCurrentUser
      if (user.isDefined) {

        // Check that the network is real
        val network = Network.findById(id)
        if (network.isDefined) {

          // Make sure the user is part of the network
          if (user.get.hasNetwork(network.get)) {

            // Create the status update
            val statusUpdate = request.body("statusUpdate")(0)
            ActivityStream.createStatusUpdate(user.get, statusUpdate, network.get.objId).save
            Ok // TODO: Redirect with message

          } else // Not part of network
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

        // Check that the network is real
        val network = Network.findById(id)
        if (network.isDefined) {

          // Make sure the user is part of the network
          if (user.get.hasNetwork(network.get)) {

            // Handle the image upload
            val file = request.body.file("image").get
            val id = user.get.username + "-" + Hasher.md5Hex(file.filename + new Date().getTime)
            val uri = ImageUploader.upload(file, id, file.contentType.get)
            val name = request.body.dataParts("name")(0)
            val image = Image(NotAssigned, name, uri).setProperty("filename", file.filename).save

            // Create the update
            ActivityStream.createImagePost(user.get, image, network.get.objId).save
            Ok(uri) // TODO: Redirect with message

          } else // Not part of network
            Ok // TODO: Redirect with message
        } else // Network doesn't exist
          Ok // TODO: Redirect with message
      } else // User not logged in
        Redirect(routes.Application.index()).flashing("alert" -> "You are not logged in")
  }

  def browse = Action {
    implicit request =>

    // Check that the user is logged in
      val user = Account.getCurrentUser
      if (user.isDefined) {
        val page: Int = request.queryString.get("page").getOrElse(Seq("0"))(0).toInt
        val networks = Network.list(page)
        Ok // TODO: Create view

      } else // User not logged in
        Redirect(routes.Application.index()).flashing("alert" -> "You are not logged in")
  }

  def networkFeed(id: Long) = Action {
    implicit request =>

    // Check that the user is logged in
      val user = Account.getCurrentUser
      if (user.isDefined) {

        // Check that the network is real
        val network = Network.findById(id)
        if (network.isDefined) {
          Ok // TODO: Create view

        } else // Network doesn't exist
          Ok // TODO: Redirect with message
      } else // User not logged in
        Redirect(routes.Application.index()).flashing("alert" -> "You are not logged in")
  }

  def joinNetwork(id: Long) = Action {
    implicit request =>

    // Check that the user is logged in
      val user = Account.getCurrentUser
      if (user.isDefined) {

        // Check that the network is real
        val network = Network.findById(id)
        if (network.isDefined) {
          user.get.addNetwork(network.get).save
          Ok // TODO: Redirect with message

        } else // Network doesn't exist
          Ok // TODO: Redirect with message
      } else // User not logged in
        Redirect(routes.Application.index()).flashing("alert" -> "You are not logged in")
  }

}
