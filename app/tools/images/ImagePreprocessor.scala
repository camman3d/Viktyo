package tools.images

import play.api.mvc.MultipartFormData.FilePart
import play.api.libs.Files.TemporaryFile
import java.io.{ByteArrayInputStream, ByteArrayOutputStream, InputStream}
import ij.process.ImageProcessor
import javax.imageio.ImageIO
import org.apache.commons.io.FilenameUtils
import ij.{ImagePlus, IJ}
import java.awt.Rectangle

/**
 * Created with IntelliJ IDEA.
 * User: Josh
 * Date: 2/14/13
 * Time: 7:18 AM
 * To change this template use File | Settings | File Templates.
 */
abstract class ImagePreprocessor {
  def process(file: FilePart[TemporaryFile], name: String): (InputStream, Long)

  def getImageProcessor(file: FilePart[TemporaryFile]): (ImageProcessor, ImagePlus) = {
    val ij = IJ.openImage(file.ref.file.getAbsolutePath)
    val imageProcessor = ij.getProcessor
    imageProcessor.setInterpolationMethod(ImageProcessor.BILINEAR)
    (imageProcessor, ij)
  }

  def imageProcessorToInputStream(imageProcessor: ImageProcessor, file: FilePart[TemporaryFile]): (InputStream, Long) = {
    val bufferedImage = imageProcessor.getBufferedImage
    val output = new ByteArrayOutputStream()
    val extension = FilenameUtils.getExtension(file.filename)
    ImageIO.write(bufferedImage, extension, output)
    val byteArray = output.toByteArray
    (new ByteArrayInputStream(byteArray), byteArray.size.toLong)
  }
}

class StandardPicturePreprocessor extends ImagePreprocessor {
  def process(file: FilePart[TemporaryFile], name: String) = {
    var (imageProcessor, ij) = getImageProcessor(file)

    // Resize
    val newWidth = math.min(ij.getWidth, 800)
    imageProcessor = imageProcessor.resize(newWidth)

    imageProcessorToInputStream(imageProcessor, file)
  }
}

class ProfilePicturePreprocessor extends ImagePreprocessor {
  def process(file: FilePart[TemporaryFile], name: String) = {
    var (imageProcessor, ij) = getImageProcessor(file)

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

    imageProcessorToInputStream(imageProcessor, file)
  }
}