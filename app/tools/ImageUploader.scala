package tools

import java.io.File
import play.api.Play
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.duration._
import play.api.mvc.MultipartFormData.FilePart
import play.api.libs.Files.TemporaryFile
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import org.apache.commons.io.FilenameUtils

/**
 * Created with IntelliJ IDEA.
 * User: camman3d
 * Date: 6/28/12
 * Time: 2:31 PM
 * To change this template use File | Settings | File Templates.
 */

object ImageUploader {

  def uploadAmazon(file: FilePart[TemporaryFile], id: String, contentType: String): String = {
    val s3 = new AmazonS3Client(new AWSCredentials {
      def getAWSSecretKey = Play.application.configuration.getString("amazon.secretKey").get
      def getAWSAccessKeyId = Play.application.configuration.getString("amazon.accessKeyId").get
    })
    val bucket = Play.application.configuration.getString("amazon.bucket").get
    val extension = "." + FilenameUtils.getExtension(file.filename)

    Akka.system.scheduler.scheduleOnce(0 seconds) {
      s3.putObject(bucket, id + extension, file.ref.file)
      s3.setObjectAcl(bucket, id + extension, CannedAccessControlList.PublicRead)
    }

    // Return the URL
    "https://s3.amazonaws.com/" + bucket + "/" + id + extension
  }


  def upload(file: FilePart[TemporaryFile], id: String, contentType: String): String = {
    val dest = Play.application.configuration.getString("imageUploader.destination").get

    if (dest == "amazon")
      uploadAmazon(file, id, contentType)
    else
      ""
  }
}
