package tools.social

import models._

/**
 * Created with IntelliJ IDEA.
 * User: Josh
 * Date: 2/7/13
 * Time: 6:42 AM
 * To change this template use File | Settings | File Templates.
 */
object PostingActions {
  def userComments(user: User, comment: String, posting: Posting) {
    ActivityStream.createComment(user, comment, posting.objId).save
  }

  def userDeletesComment(comment: Text) {
    ActivityStream.listByObject(comment.id.get)(0).delete()
  }

  def userFavorites(user: User, posting: Posting) {

  }

  def userUnfavorites(user: User, posting: Posting) {

  }

  def userPostsImage(user: User, image: Image, posting: Posting) {
    ActivityStream.createImagePost(user, image, posting.objId).save
  }

  def userRemovesImage(user: User, image: Image) {

  }

}
