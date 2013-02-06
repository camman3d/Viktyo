import anorm.NotAssigned
import java.util.Date
import models.{Location, Posting, User, ViktyoConfiguration}
import play.api._
import tools.{Panoramio, Hasher}

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    createFixtures()
  }

  def createFixtures() {
    // TODO: Create fixtures
    Logger.info("Creating fixtures")

    // Configuration
    if(!ViktyoConfiguration.findByName("signup.user.availableFields").isDefined)
      ViktyoConfiguration(NotAssigned, "signup.user.availableFields", Right(List("Email", "Gender", "Test Select", "Test Checkbox"))).save
    if(!ViktyoConfiguration.findByName("signup.user.availableFieldsTypes").isDefined)
      ViktyoConfiguration(NotAssigned, "signup.user.availableFieldsTypes", Right(List("text", "radio[\"Male\",\"Female\"]", "select[\"One\",\"Two\",\"Three\"]", "checkbox"))).save
    if(!ViktyoConfiguration.findByName("signup.user.requiredFields").isDefined)
      ViktyoConfiguration(NotAssigned, "signup.user.requiredFields", Right(List("Email", "Gender"))).save
    if(!ViktyoConfiguration.findByName("signup.organization.availableFields").isDefined)
      ViktyoConfiguration(NotAssigned, "signup.organization.availableFields", Right(List("Email", "Website"))).save
    if(!ViktyoConfiguration.findByName("signup.organization.availableFieldsTypes").isDefined)
      ViktyoConfiguration(NotAssigned, "signup.organization.availableFieldsTypes", Right(List("text", "text"))).save
    if(!ViktyoConfiguration.findByName("signup.organization.requiredFields").isDefined)
      ViktyoConfiguration(NotAssigned, "signup.organization.requiredFields", Right(List("Email"))).save

    // Test data
    if(!ViktyoConfiguration.findByName("testdata.created").isDefined) {
      ViktyoConfiguration(NotAssigned, "testdata.created", Left("true")).save

      // Create the users
      User(NotAssigned, "Test user 1", "test1", Hasher.sha256Base64("test123")).setProperty("accountType", "user").save
      User(NotAssigned, "Test user 2", "test2", Hasher.sha256Base64("test123")).setProperty("accountType", "user").save
      User(NotAssigned, "Test user 3", "test3", Hasher.sha256Base64("test123")).setProperty("accountType", "user").save
      User(NotAssigned, "Test user 4", "test4", Hasher.sha256Base64("test123")).setProperty("accountType", "user").save
      User(NotAssigned, "Test user 5", "test5", Hasher.sha256Base64("test123")).setProperty("accountType", "user").save
      User(NotAssigned, "Test user 6", "test6", Hasher.sha256Base64("test123")).setProperty("accountType", "user").save
      User(NotAssigned, "Test user 7", "test7", Hasher.sha256Base64("test123")).setProperty("accountType", "user").save
      User(NotAssigned, "Test user 8", "test8", Hasher.sha256Base64("test123")).setProperty("accountType", "user").save
      User(NotAssigned, "Test user 9", "test9", Hasher.sha256Base64("test123")).setProperty("accountType", "user").save
      User(NotAssigned, "Test user 10", "test10", Hasher.sha256Base64("test123")).setProperty("accountType", "user").save

      // Create the organizations
      val org1 = User(NotAssigned, "Test organization 1", "org1", Hasher.sha256Base64("test123")).setProperty("accountType", "organization").save
      val org2 = User(NotAssigned, "Test organization 2", "org2", Hasher.sha256Base64("test123")).setProperty("accountType", "organization").save
      val org3 = User(NotAssigned, "Test organization 3", "org3", Hasher.sha256Base64("test123")).setProperty("accountType", "organization").save
      val org4 = User(NotAssigned, "Test organization 4", "org4", Hasher.sha256Base64("test123")).setProperty("accountType", "organization").save
      val org5 = User(NotAssigned, "Test organization 5", "org5", Hasher.sha256Base64("test123")).setProperty("accountType", "organization").save
      val org6 = User(NotAssigned, "Test organization 6", "org6", Hasher.sha256Base64("test123")).setProperty("accountType", "organization").save
      val org7 = User(NotAssigned, "Test organization 7", "org7", Hasher.sha256Base64("test123")).setProperty("accountType", "organization").save
      val org8 = User(NotAssigned, "Test organization 8", "org8", Hasher.sha256Base64("test123")).setProperty("accountType", "organization").save
      val org9 = User(NotAssigned, "Test organization 9", "org9", Hasher.sha256Base64("test123")).setProperty("accountType", "organization").save
      val org10 = User(NotAssigned, "Test organization 10", "org10", Hasher.sha256Base64("test123")).setProperty("accountType", "organization").save

      // Create the admin
      User(NotAssigned, "VIKTYO Admin", "admin", Hasher.sha256Base64("Viktyo2525")).setProperty("accountType", "admin").save

      // Create the postings
      val time = new Date().getTime
      Posting(NotAssigned, "Internship in Provo", time, org1, Location(NotAssigned, "Provo, UT, USA", 40.231315, -111.659546))
        .setProperty("postingType", "internship")
        .setProperty("panoramio", Panoramio.getImages(40.231315, -111.659546, 0, 1)(0).toString()).save
      Posting(NotAssigned, "Internship in New York", time, org2, Location(NotAssigned, "New York, NY, USA", 40.713956, -74.025879))
        .setProperty("postingType", "internship")
        .setProperty("panoramio", Panoramio.getImages(40.713956, -74.025879, 0, 1)(0).toString()).save
      Posting(NotAssigned, "Internship in Cairo", time, org3, Location(NotAssigned, "Cairo, Egypt", 30.145127, 31.201172))
        .setProperty("postingType", "internship")
        .setProperty("panoramio", Panoramio.getImages(30.145127, 31.201172, 0, 1)(0).toString()).save
      Posting(NotAssigned, "Job in Paris", time, org4, Location(NotAssigned, "Paris, France", 48.864715, 2.373047))
        .setProperty("postingType", "job")
        .setProperty("panoramio", Panoramio.getImages(48.864715, 2.373047, 0, 1)(0).toString()).save
      Posting(NotAssigned, "Job in London", time, org5, Location(NotAssigned, "London, UK", 51.522416, -0.131836))
        .setProperty("postingType", "job")
        .setProperty("panoramio", Panoramio.getImages(51.522416, -0.131836, 0, 1)(0).toString()).save
      Posting(NotAssigned, "Study Abroad in Madrid", time, org6, Location(NotAssigned, "Madrid, Spain", 40.446947, -3.713379))
        .setProperty("postingType", "study_abroad").save
      Posting(NotAssigned, "Study Abroad in Istanbul", time, org7, Location(NotAssigned, "Istanbul, Turkey", 41.004775, 28.981934))
        .setProperty("postingType", "study_abroad").save
      Posting(NotAssigned, "Volunteer in Tokyo", time, org8, Location(NotAssigned, "Tokyo, Japan", 35.684072, 139.691162))
        .setProperty("postingType", "volunteer").save
      Posting(NotAssigned, "Volunteer in Sydney", time, org9, Location(NotAssigned, "Sydney, Australia", -33.870416, 151.171875))
        .setProperty("postingType", "volunteer").save
      Posting(NotAssigned, "Volunteer in Moscow", time, org10, Location(NotAssigned, "Moscow, Russia", 55.776573, 37.529297))
        .setProperty("postingType", "volunteer").save

    }
  }
}
