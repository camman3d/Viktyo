package tools

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, InputStream, File}
import play.api.Play
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
import ij.IJ
import ij.process.ImageProcessor
import java.awt.Rectangle
import javax.imageio.ImageIO

/**
 * Created with IntelliJ IDEA.
 * User: camman3d
 * Date: 6/28/12
 * Time: 2:31 PM
 * To change this template use File | Settings | File Templates.
 */

object ImageUploader {

  def uploadAmazon(filename: String, inputStream: InputStream, contentType: String, contentLength: Long): String = {
    val s3 = new AmazonS3Client(new AWSCredentials {
      def getAWSSecretKey = Play.application.configuration.getString("amazon.secretKey").get
      def getAWSAccessKeyId = Play.application.configuration.getString("amazon.accessKeyId").get
    })
    val bucket = Play.application.configuration.getString("amazon.bucket").get
    val metadata = new ObjectMetadata()
    metadata.setContentType(contentType)
    metadata.setContentLength(contentLength)

    Akka.system.scheduler.scheduleOnce(0 seconds) {
      s3.putObject(bucket, filename, inputStream, metadata)
      s3.setObjectAcl(bucket, filename, CannedAccessControlList.PublicRead)
    }

    // Return the URL
    "https://s3.amazonaws.com/" + bucket + "/" + filename
  }



  def upload(filename: String, inputStream: InputStream, contentType: String, contentLength: Long): String = {
    val dest = Play.application.configuration.getString("imageUploader.destination").get

    if (dest == "amazon")
      uploadAmazon(filename, inputStream, contentType, contentLength)
    else
      ""
  }

  def uploadPicture(file: FilePart[TemporaryFile], name: String)(implicit user: User): Image = {
    // Set up to process the image
    val ij = IJ.openImage(file.ref.file.getAbsolutePath)
    var imageProcessor = ij.getProcessor
    imageProcessor.setInterpolationMethod(ImageProcessor.BILINEAR)

    // Resize
    val newWidth = math.min(ij.getWidth, 800)
    imageProcessor = imageProcessor.resize(newWidth)

    val bufferedImage = imageProcessor.getBufferedImage
    val id = user.username + "-" + Hasher.md5Hex(file.filename + new Date().getTime)
    val extension = FilenameUtils.getExtension(file.filename)
    val filename = id + "." + extension
    val output = new ByteArrayOutputStream()
    ImageIO.write(bufferedImage, extension, output)
    val byteArray = output.toByteArray
    val inputStream = new ByteArrayInputStream(byteArray)

    // Upload the image
    val uri = upload(filename, inputStream, file.contentType.get, byteArray.length.toLong)

    Image(NotAssigned, name, uri).setProperty("filename", file.filename).save
  }

  def uploadProfilePicture(file: FilePart[TemporaryFile], name: String)(implicit user: User): Image = {
    // Set up to process the image
    val ij = IJ.openImage(file.ref.file.getAbsolutePath)
    var imageProcessor = ij.getProcessor
    imageProcessor.setInterpolationMethod(ImageProcessor.BILINEAR)
    val width = ij.getWidth
    val height = ij.getHeight

    // Resize
    val ratio = 150.0 / height.toDouble
    val newWidth = (width * ratio).toInt
    imageProcessor = imageProcessor.resize(newWidth)

    // Crop
    val x = ((newWidth - 150) / 2).toInt
    val cropRegion = new Rectangle(x, 0, 150, 150)
    imageProcessor.setRoi(cropRegion)
    imageProcessor = imageProcessor.crop()

    val bufferedImage = imageProcessor.getBufferedImage
    val id = user.username + "-" + Hasher.md5Hex(file.filename + new Date().getTime)
    val extension = FilenameUtils.getExtension(file.filename)
    val filename = id + "." + extension
    val output = new ByteArrayOutputStream()
    ImageIO.write(bufferedImage, extension, output)
    val byteArray = output.toByteArray
    val inputStream = new ByteArrayInputStream(byteArray)

    // Upload the image
    val uri = upload(filename, inputStream, file.contentType.get, byteArray.length.toLong)

    Image(NotAssigned, name, uri).setProperty("filename", file.filename).save
  }
}
