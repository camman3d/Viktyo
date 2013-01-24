package tools

import play.api.Play
import play.api.Play.current
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import org.codemonkey.simplejavamail.{TransportStrategy, Mailer, Email}
import javax.mail.Message.RecipientType
import scala.concurrent.duration._

/**
 * Created with IntelliJ IDEA.
 * User: Josh
 * Date: 1/24/13
 * Time: 10:28 AM
 * To change this template use File | Settings | File Templates.
 */
object Emailer {
  def sendEmail(toName: String, toEmail: String, subject: String, body: String) = {
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

      new Mailer(host, port.toInt, address, password, TransportStrategy.SMTP_SSL).sendMail(email)
    }
  }
}
