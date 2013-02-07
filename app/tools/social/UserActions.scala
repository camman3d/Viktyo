package tools.social

import models.{Image, ActivityStream, ViktyoNotification, User}

/**
 * Created with IntelliJ IDEA.
 * User: camman3d
 * Date: 2/7/13
 * Time: 10:20 AM
 * To change this template use File | Settings | File Templates.
 */
object UserActions {
  def userFollows(user: User, otherUser: User) {
    user.addFollowing(otherUser).save
    otherUser.addFollower(user).save
    ViktyoNotification.createFollowing(user, otherUser).save
    ActivityStream.generate.users.follow(user, otherUser).save
  }

  def userUnfollows(user: User, otherUser: User) {
    user.removeFollowing(otherUser).save
    otherUser.removeFollower(user).save
    ActivityStream.find(user, "follow", otherUser.objId, user.objId).get.delete()
  }

  def userSetProfilePicture(user: User, image: Image) {
    user.setProperty("profilePicture", image.uri).save
    ActivityStream.generate.users.profilePicture(user, image).save
  }

  def userPostsStatusUpdate(user: User, statusUpdate: String) {
    ActivityStream.generate.general.statusUpdate(user, statusUpdate, user.objId).save
  }

  def userPostsImage(user: User, image: Image) {
    ActivityStream.generate.general.imagePost(user, image, user.objId).save
  }
}
