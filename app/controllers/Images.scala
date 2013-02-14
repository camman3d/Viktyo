package controllers

import play.api.mvc.Controller
import tools.images.ImageUploader
import play.api.libs.json.Json
import concurrent.ExecutionContext
import ExecutionContext.Implicits.global

/**
 * Created with IntelliJ IDEA.
 * User: Josh
 * Date: 2/14/13
 * Time: 7:10 AM
 * To change this template use File | Settings | File Templates.
 */
object Images extends Controller {

  def upload = Account.AuthenticatedAction {
    request =>
      implicit user =>
        // Handle the image upload
        val file = request.body.asMultipartFormData.get.file("image").get
        val name = request.body.asMultipartFormData.get.dataParts("name")(0)
        val purpose = Symbol(request.body.asMultipartFormData.get.dataParts.get("purpose").map(_(0)).getOrElse("std"))
        val image = ImageUploader.uploadPicture(file, name, purpose)

        // Async result
        Async {
          image.map(image =>
            Ok(Json.obj("success" -> true, "imageId" -> image.id.get))
          )
        }
  }

}
