package tools

import play.api.libs.json.JsValue
import play.api.libs.ws.WS
import concurrent.duration._
import concurrent.{ExecutionContext, Await}
import ExecutionContext.Implicits.global
import play.api.Logger

object Panoramio {

  /* Panoramio photo json has the following properties
   *  - height
   *  - latitude
   *  - longitude
   *  - owner_id
   *  - owner_name
   *  - owner_url
   *  - photo_file_url
   *  - photo_id
   *  - photo_title
   *  - photo_url
   *  - upload_date
   *  - width
   */

  def getImages(latitude: Double, longitude: Double, start: Int = 0, end: Int = 20, range: Double = 0.1, size: String = "medium"): List[JsValue] = {
    val minx = longitude - range
    val maxx = longitude + range
    val miny = latitude - range
    val maxy = latitude + range
    val url = "http://www.panoramio.com/map/get_panoramas.php?set=public&from=" + start + "&to=" + end +
      "&minx=" + minx + "&miny=" + miny + "&maxx=" + maxx + "&maxy=" + maxy + "&size=" + size + "&mapfilter=true"

    val request = WS.url(url).get().map { r => (r.json \ "photos").as[List[JsValue]] }
    Await.result(request, 5 seconds)
  }
}
