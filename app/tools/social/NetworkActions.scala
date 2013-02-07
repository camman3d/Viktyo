package tools.social

import models._

/**
 * Social network actions dealing with Networks
 * @author Josh Monson
 */
object NetworkActions {
  def userJoins(user: User, network: Network) {
    user.addNetwork(network).save
    network.addMember(user).save
    ActivityStream.generate.networks.join(user, network).save
  }

  def userLeaves(user: User, network: Network) {
    user.removeNetwork(network).save
    network.removeMember(user).save
    ActivityStream.find(user, "join", network.objId, network.objId).get.delete()
  }

  def userPostsStatusUpdate(user: User, statusUpdate: String, network: Network) {
    ActivityStream.generate.general.statusUpdate(user, statusUpdate, network.objId).save
  }

  def userPostsImage(user: User, image: Image, network: Network) {
    ActivityStream.generate.general.imagePost(user, image, network.objId).save
  }
}
