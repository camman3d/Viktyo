package tools.social

import models._

/**
 * Social network actions dealing with Postings
 * @author Josh Monson
 */
object PostingActions {
  def userComments(user: User, comment: String, posting: Posting) {
    ActivityStream.generate.general.comment(user, comment, posting.objId).save
  }

  def userFavorites(user: User, posting: Posting) {
    user.addFavorite(posting).save
    posting.addFavorite(user).save
    ActivityStream.generate.postings.favorite(user, posting).save
  }

  def userUnfavorites(user: User, posting: Posting) {
    user.removeFavorite(posting).save
    posting.removeFavorite(user).save
    ActivityStream.find(user, "favorite", posting.objId, posting.objId).get.delete()
  }

  def userFollows(user: User, posting: Posting) {
    posting.addFollower(user).save
    ActivityStream.generate.postings.follow(user, posting).save
  }

  def userUnfollows(user: User, posting: Posting) {
    posting.removeFollower(user).save
    ActivityStream.find(user, "follow", posting.objId, posting.objId).get.delete()
  }

  def userPostsImage(user: User, image: Image, posting: Posting) {
    ActivityStream.generate.general.imagePost(user, image, posting.objId).save
  }

  def orgCreates(user: User, posting: Posting) {
    ActivityStream.generate.postings.create(user, posting)
  }

  def orgUpdatesCover(user: User, posting: Posting, image: Image) {
    ActivityStream.generate.postings.updatedCover(user, posting, image)
  }

}
