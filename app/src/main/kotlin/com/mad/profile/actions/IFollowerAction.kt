package com.mad.profile.actions

import java.util.*

interface IFollowerAction {
  suspend fun follow(follower: UUID, followee: UUID): Boolean
  suspend fun unfollow(follower: UUID, followee: UUID): Boolean
  suspend fun listFollowers(user: UUID): List<UUID>
  suspend fun listFollowing(user: UUID): List<UUID>
  suspend fun isFollowing(follower: UUID, followee: UUID): Boolean
}
