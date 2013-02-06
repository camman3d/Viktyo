package tools

import java.util.Date
import org.joda.time.{Duration, Period, DateTime}

/**
 * Created with IntelliJ IDEA.
 * User: camman3d
 * Date: 5/2/12
 * Time: 9:37 AM
 * To change this template use File | Settings | File Templates.
 */

object TimeTools {
  def dateFromTimestamp(timestamp: Long): String = {
    val d = new Date(timestamp)
    d.toString
  }


  def getFeedTime(timestamp: Long): String = {
    val duration = new Duration(timestamp, new Date().getTime)

    if (duration.getStandardSeconds < 20)
      "Just now"
    else if (duration.getStandardSeconds < 60)
      "A minute ago"
    else if (duration.getStandardMinutes < 60)
      duration.getStandardMinutes + " minutes ago"
    else if (duration.getStandardHours < 24)
      duration.getStandardHours + " hours ago"
    else if (duration.getStandardDays < 31)
      duration.getStandardDays + " days ago"
    else {
      val date = new DateTime(timestamp)
      "On " + date.toString("MMM dd, yyyy")
    }
  }
}
