package tools.social

import models._

/**
 * Created with IntelliJ IDEA.
 * User: Josh
 * Date: 2/7/13
 * Time: 6:31 AM
 * To change this template use File | Settings | File Templates.
 */
object NetworkActions {
  def userJoins(user: User, network: Network) {
    user.addNetwork(network).save
    network.addMember(user).save
    ActivityStream.createNetworkJoin(user, network).save
  }

  def userLeaves(user: User, network: Network) {
    user.removeNetwork(network).save
    network.removeMember(user).save
  }

  def userPostsStatusUpdate(user: User, statusUpdate: String, network: Network) {
    ActivityStream.createStatusUpdate(user, statusUpdate, network.objId).save
  }

  def userDeletesStatusUpdate(update: Text) {
    ActivityStream.listByObject(update.id.get)(0).delete()
  }

  def userPostsImage(user: User, image: Image, network: Network) {
    ActivityStream.createImagePost(user, image, network.objId).save
  }
}
