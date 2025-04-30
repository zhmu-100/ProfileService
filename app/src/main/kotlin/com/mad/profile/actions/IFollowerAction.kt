package com.mad.profile.actions

interface IFollowerAction {
  suspend fun follow(followerId: String, followeeId: String): Boolean
  suspend fun unfollow(followerId: String, followeeId: String): Boolean
  suspend fun listFollowers(userId: String): List<String>
  suspend fun listFollowing(userId: String): List<String>
  suspend fun isFollowing(followerId: String, followeeId: String): Boolean
}
