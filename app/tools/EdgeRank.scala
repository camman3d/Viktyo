package tools

import models.{ActivityStream, User}

/**
 * Created with IntelliJ IDEA.
 * User: Josh
 * Date: 1/24/13
 * Time: 11:56 AM
 * To change this template use File | Settings | File Templates.
 */
object EdgeRank {

  def combineStreams(streams: List[List[ActivityStream]]): List[ActivityStream] =
    streams.foldLeft[List[ActivityStream]](List())((l1, l2) => l1 ::: l2)

  def getFeed(user: User): List[ActivityStream] = {
    // TODO: Program EdgeRank algorithm
    // For now, just give the user's activity and that of those he's following

    val myStream = ActivityStream.listByActor(user.id.get)
    val following = getFollowing(user)
    sortStream(combineStreams(myStream :: following))
  }

  def getFollowing(user: User): List[List[ActivityStream]] = {
    if (user.getProperty("following").isDefined)
      user.getProperty("following").get.split(",").map(s => ActivityStream.listByActor(s.toLong)).toList
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
}
