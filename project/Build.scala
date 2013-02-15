import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "viktyo"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
    "mysql" % "mysql-connector-java" % "5.1.18",
    "org.apache.commons" % "commons-lang3" % "3.0",
    "org.apache.commons" % "commons-io" % "1.3.2",
    "org.codemonkey.simplejavamail" % "simple-java-mail" % "2.1",
    "com.amazonaws" % "aws-java-sdk" % "1.3.27",
    "gov.nih.imagej" % "imagej" % "1.46",
    "joda-time" % "joda-time" % "2.1"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
