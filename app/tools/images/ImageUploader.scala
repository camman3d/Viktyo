package tools.images

import java.io.InputStream
import play.api.{Logger, Play}
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.duration._
import play.api.mvc.MultipartFormData.FilePart
import play.api.libs.Files.TemporaryFile
import com.amazonaws.services.s3.model.{ObjectMetadata, CannedAccessControlList}
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import org.apache.commons.io.FilenameUtils
import models.{User, Image}
import java.util.Date
import anorm.NotAssigned
import tools.Hasher
import concurrent.{Future, future}

/**
 * Created with IntelliJ IDEA.
 * User: camman3d
 * Date: 6/28/12
 * Time: 2:31 PM
 * To change this template use File | Settings | File Templates.
 */

object ImageUploader {

  def uploadAmazon(filename: String, inputStream: InputStream, contentType: String, contentLength: Long): Future[String] = {
    val s3 = new AmazonS3Client(new AWSCredentials {
      def getAWSSecretKey = Play.application.configuration.getString("amazon.secretKey").get
      def getAWSAccessKeyId = Play.application.configuration.getString("amazon.accessKeyId").get
    })
    val bucket = Play.application.configuration.getString("amazon.bucket").get
    val metadata = new ObjectMetadata()
    metadata.setContentType(contentType)
    metadata.setContentLength(contentLength)

    future {
      s3.putObject(bucket, filename, inputStream, metadata)
      s3.setObjectAcl(bucket, filename, CannedAccessControlList.PublicRead)

      // Return the URL
      "https://s3.amazonaws.com/" + bucket + "/" + filename
    }
  }



  def upload(filename: String, inputStream: InputStream, contentType: String, contentLength: Long): Future[String] = {
    val dest = Play.application.configuration.getString("imageUploader.destination").get

    if (dest == "amazon")
      uploadAmazon(filename, inputStream, contentType, contentLength)
    else {
      Logger.error("Unknown image upload technique")
      future {""}
    }
  }

  def uploadPicture(file: FilePart[TemporaryFile], name: String, purpose: Symbol)(implicit user: User): Future[Image] = {
    val imagePreprocessor = {
      if (purpose == 'profile)
        new ProfilePicturePreprocessor()
      else
        new StandardPicturePreprocessor()
    }

    // Process the image
    val (inputStream, length) = imagePreprocessor.process(file, name)

    // Upload the image
    val id = user.username + "-" + Hasher.md5Hex(file.filename + new Date().getTime)
    val extension = FilenameUtils.getExtension(file.filename)
    val filename = id + "." + extension
    val uri = upload(filename, inputStream, file.contentType.get, length)

    // Return a future image
    uri.map(url =>
      Image(NotAssigned, name, url)
        .setProperty("filename", file.filename)
        .setProperty("owner", user.id.get.toString)
        .save
    )
  }
}
