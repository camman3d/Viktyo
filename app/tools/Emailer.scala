package tools

import play.api.Play
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import org.codemonkey.simplejavamail.{TransportStrategy, Mailer, Email}
import javax.mail.Message.RecipientType
import scala.concurrent.duration._
import models.User
import controllers.routes
import play.api.mvc.RequestHeader

/**
 * Created with IntelliJ IDEA.
 * User: Josh
 * Date: 1/24/13
 * Time: 10:28 AM
 * To change this template use File | Settings | File Templates.
 */
object Emailer {
  def sendEmail(toName: String, toEmail: String, subject: String, body: String, bodyHtml: String) = {
    // Schedule the email as to not slow down the request
    Akka.system.scheduler.scheduleOnce(1 second) {
      val email = new Email

      val host = Play.configuration.getString("smtp.host").get
      val port = Play.configuration.getString("smtp.port").get
      val name = Play.configuration.getString("smtp.name").get
      val address = Play.configuration.getString("smtp.address").get
      val password = Play.configuration.getString("smtp.password").get

      email.setFromAddress(name, address)
      email.setSubject(subject)
      email.addRecipient(toName, toEmail, RecipientType.TO)
      email.setText(body)
      email.setTextHTML(bodyHtml)

      new Mailer(host, port.toInt, address, password, TransportStrategy.SMTP_SSL).sendMail(email)
    }
  }

  def sendPasswordRecoveryEmail(user: User)(implicit request: RequestHeader) = {
    val name = user.fullname
    val email = user.getProperty("email").get
    val subject = "VIKTYO Password Recovery"
    val url = routes.Account.passwordResetPage(user.getProperty("emailResetCode").get).absoluteURL()
    val body = "Greetings " + name + ",\r\n\r\n" +
      "Don't fret over the forgotten password. Everyone does it. All you have to do to reset it is click on the link below:\r\n" +
      url + "\r\n\r\nCheers,\r\nVIKTYO Admin"
    val bodyHtml = "<p>Greetings" + name + ",</p>" +
      "<p>Don't fret over the forgotten password. Everyone does it. All you have to do to reset it is click on the link below:</p>" +
      "<p><a href=\"" + url + "\">" + url + "</a></p>" +
      "<p>Cheers,<br>VIKTYO Admin</p>"

    sendEmail(name, email, subject, body, bodyHtml)
  }
}
