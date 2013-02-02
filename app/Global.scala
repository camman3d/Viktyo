import anorm.NotAssigned
import models.ViktyoConfiguration
import play.api._

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    createFixtures()
  }

  def createFixtures() {
    // TODO: Create fixtures
    Logger.info("Creating fixtures")

    // Configuration
    if(!ViktyoConfiguration.findByName("signup.user.availableFields").isDefined)
      ViktyoConfiguration(NotAssigned, "signup.user.availableFields", Right(List("Email", "Website"))).save
    if(!ViktyoConfiguration.findByName("signup.user.availableFieldsTypes").isDefined)
      ViktyoConfiguration(NotAssigned, "signup.user.availableFieldsTypes", Right(List("text", "text"))).save
    if(!ViktyoConfiguration.findByName("signup.user.requiredFields").isDefined)
      ViktyoConfiguration(NotAssigned, "signup.user.requiredFields", Right(List("Email"))).save
    if(!ViktyoConfiguration.findByName("signup.organization.availableFields").isDefined)
      ViktyoConfiguration(NotAssigned, "signup.organization.availableFields", Right(List("Email", "Website"))).save
    if(!ViktyoConfiguration.findByName("signup.organization.availableFieldsTypes").isDefined)
      ViktyoConfiguration(NotAssigned, "signup.organization.availableFieldsTypes", Right(List("text", "text"))).save
    if(!ViktyoConfiguration.findByName("signup.organization.requiredFields").isDefined)
      ViktyoConfiguration(NotAssigned, "signup.organization.requiredFields", Right(List("Email"))).save
  }
}
