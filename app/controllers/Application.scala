package controllers

import play.api._
import play.api.mvc._
import models._
import anorm.NotAssigned
import java.util.Date
import org.apache.commons.lang3.StringEscapeUtils

object Application extends Controller {
  
  def index = Action {
    //Ok(views.html.index("Your new application is ready."))

    //val property = Property(NotAssigned, "one", "two",)

//    val user = User(NotAssigned, "Josh Monson", "camman3d", "pass").setProperty("one", "two").setProperty("three", "four").save
//    val user = User.findById(5).get

    //val posting = Posting(NotAssigned, "Intership!", 123456, user, Location(NotAssigned, "South Jordan", 5.6, 7.8)).save
    //val posting = Posting.findById(2)

    //val image = Image(NotAssigned, "Some Image", "http://example.com/img.jpg").setProperty("color", "greyscale").save
//    val image = Image.findById(2)

//    val text = Text(NotAssigned, "Hey there").save
//    val text = Text.findById(1).get

//    val as = ActivityStream(NotAssigned, new Date().getTime, user, "posted", text.objId, user.objId).save

//    val user = User(NotAssigned, "Joshy boy", "camman3d", "pass").setProperty("accountType", "user").save
//    val users = User.listByAccountType("user")

    //val network = Network(NotAssigned, "Arabic", "Something").setProperty("language", "ar").save
//    val strings = List(
//      "Josh said \"That's mine, you know!\"",
//      "Another String"
//    )
//    val es = strings.map(s => StringEscapeUtils.escapeCsv(s)).mkString(",")
//
//    val strings2 = es.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)").map(s => StringEscapeUtils.unescapeCsv(s)).toList

//    val config1 = ViktyoConfiguration(NotAssigned, "homepage.title", Left("Something")).save
//    val config2 = ViktyoConfiguration(NotAssigned, "homepage.menus", Right(List("Something else", "Another thing"))).save

    val s1 = StringEscapeUtils.escapeCsv("this a s1")
    val s2 = StringEscapeUtils.escapeCsv("this is, s2")

    Ok(s1 + "\n" + s2)
  }
  
}