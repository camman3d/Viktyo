package tools

import models._
import play.api.templates.Html

/**
 * Created with IntelliJ IDEA.
 * User: Josh
 * Date: 1/24/13
 * Time: 11:56 AM
 * To change this template use File | Settings | File Templates.
 */
object FeedTools {

  def combineStreams(streams: List[List[ActivityStream]]): List[ActivityStream] =
    streams.foldLeft[List[ActivityStream]](List())((l1, l2) => l1 ::: l2)

  def getFeed(user: User): List[ActivityStream] = {
    // TODO: Program EdgeRank algorithm
    // For now, just give the user's activity and that of those he's following

    val myStream = ActivityStream.listByActor(user.id.get)
    val target = ActivityStream.listByObject(user.objId)
    sortStream(combineStreams(List(myStream, target)))
  }

  def getFollowers(user: User): List[List[ActivityStream]] = {
    if (user.getProperty("follower").isDefined)
      user.getProperty("follower").get.split(",").map(s => ActivityStream.listByActor(s.toLong)).toList
    else
      List()
  }

  /**
   * Sort newest post first
   * @param stream The stream (list of activity stream objects) to be sorted
   * @return
   */
  def sortStream(stream: List[ActivityStream]) =
    stream.sortWith((s1, s2) => s1.published > s2.published)


  val activityStreamIcons = Map(
    "comment"         -> "glyphicons_309_comments.png",
    "profilePicture"  -> "glyphicons_138_picture.png",
    "imagePost"       -> "glyphicons_138_picture.png",
    "favorite"        -> "glyphicons_049_star.png",
    "follow"          -> "glyphicons_006_user_add.png"
  )

  def getIcon(activityStream: ActivityStream): String = activityStreamIcons(activityStream.verb)

  def getContent(activityStream: ActivityStream): Html = {

    // Check the type of the activity stream entry to see what content we need to render
    if (activityStream.verb == "profilePicture")

      // Profile picture changed
      views.html.users.feedHelpers.profilePicture(activityStream.actor)
    else if (activityStream.verb == "follow") {

      // Follow. Get the object the person is following
      val target = ViktyoObject.getObjectType(activityStream.obj)
      if (target == 'user) {

        // It's another user
        val otherUser = User.findByObjId(activityStream.obj).get
        views.html.users.feedHelpers.follow(activityStream.actor, otherUser)
      } else if (target == 'posting) {

        // It's a posting
        val posting = Posting.findByObjId(activityStream.obj).get
        views.html.users.feedHelpers.followPosting(activityStream.actor, posting)
      } else
        // Undefined
        Html("following unknown object")
    } else if (activityStream.verb == "comment") {

      // Follow. Get the target the person commented on
      val target = ViktyoObject.getObjectType(activityStream.target)
      val comment = Text.findByObjId(activityStream.obj).get
      if (target == 'image) {

        // It's an image
        val image = Image.findByObjId(activityStream.target).get
        views.html.users.feedHelpers.commentPicture(activityStream.actor, image, image.getOwner, comment)
      } else if (target == 'posting) {

        // It's a posting
        val posting = Posting.findByObjId(activityStream.obj).get
        views.html.users.feedHelpers.commentPosting(activityStream.actor, posting, comment)
      } else
        // Undefined
        Html("comment on unknown object")
    } else if (activityStream.verb == "imagePost") {

      // Image post. Get the target the person posted the picture on
      val target = ViktyoObject.getObjectType(activityStream.target)
      val image = Image.findById(activityStream.obj).get
      if (target == 'user) {

        // It's a user (their own profile)
        views.html.users.feedHelpers.imagePostProfile(activityStream.actor, image)
      } else if (target == 'posting) {

        // It's a posting
        val posting = Posting.findByObjId(activityStream.obj).get
        views.html.users.feedHelpers.imagePostPosting(activityStream.actor, image, posting)
      } else
      // Undefined
        Html("posted image on unknown object")
    } else if (activityStream.verb == "favorite") {

      // Favorite
      val posting = Posting.findByObjId(activityStream.obj).get
      views.html.users.feedHelpers.favorite(activityStream.actor, posting)
    }
    else
      Html("unknown verb")
  }
}
