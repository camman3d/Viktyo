package tools

import play.api.Play
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import org.codemonkey.simplejavamail.{TransportStrategy, Mailer, Email}
import javax.mail.Message.RecipientType
import scala.concurrent.duration._
import models.{Network, User}
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

  def sendPasswordRecoveryEmail(user: User)(implicit request: RequestHeader) {
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

  def sendNetworkSuggestionEmail(user: User, network: Network)(implicit request: RequestHeader) {
    val name = Play.configuration.getString("smtp.name").get
    val email = Play.configuration.getString("smtp.address").get
    val subject = "VIKTYO Suggested network - " + network.name

    val website = network.getProperty("website").get
    val userUrl = routes.Users.view(user.id.get).absoluteURL()
    val acceptUrl = routes.Admin.networks().absoluteURL()
    val body = "The following network was suggested:\r\n" +
      "Name: " + network.name + "\r\n" +
      "Description: " + network.description + "\r\n" +
      "Website: " + website + "\r\n" +
      "Creator: " + user.fullname + "(" + userUrl + ")\r\n" +
      "Moderated: " + (!network.getProperty("moderators").get.isEmpty) + "\r\n\r\n" +
      "You can approve/reject this at the Network Moderation control panel: " + acceptUrl

    val bodyHtml = "<p>The following network was suggested:</p>" +
      "<p><strong>Name:</strong> " + network.name + "<br>" +
      "<strong>Description:</strong> " + network.description + "<br>" +
      "<strong>Website:</strong> <a href=\"" + website + "\">" + website + "</a><br>" +
      "<strong>Creator:</strong> <a href=\"" + userUrl + "\">" + user.fullname + "</a><br>" +
      "<strong>Moderated:</strong> " + (!network.getProperty("moderators").get.isEmpty) + "</p>" +
      "<p>You can approve/reject this at the <a href=\"" + acceptUrl + "\">Network Moderation control panel</a>.</p>"

    sendEmail(name, email, subject, body, bodyHtml)
  }
}
