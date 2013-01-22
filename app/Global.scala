import play.api._

class Global extends GlobalSettings {
  override def onStart(app: Application) {
    createFixtures()
  }

  def createFixtures() {
    // TODO: Create fixtures
  }
}
