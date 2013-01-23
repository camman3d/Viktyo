import play.api._

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    createFixtures()
  }

  def createFixtures() {
    // TODO: Create fixtures
  }
}
