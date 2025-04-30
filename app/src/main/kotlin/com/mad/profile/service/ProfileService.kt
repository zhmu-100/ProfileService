package com.mad.profile.service

import com.mad.profile.actions.IFollowerAction
import com.mad.profile.actions.IProfileAction
import com.mad.profile.model.*

class ProfileService(
    private val profileAction: IProfileAction,
    private val followerAction: IFollowerAction
) {
  suspend fun createProfile(request: CreateProfileRequest): UserProfile {
    val candidate = request.profile

    profileAction.getByEmail(candidate.email)?.let {
      return it
    }

    val withId =
        if (candidate.id.isBlank()) candidate.copy(id = java.util.UUID.randomUUID().toString())
        else candidate

    return profileAction.create(withId)
  }

  suspend fun getProfile(id: String): UserProfile? = profileAction.get(id)

  suspend fun listProfiles(page: Int, pageSize: Int): List<UserProfile> =
      profileAction.list(page, pageSize)

  suspend fun updateProfile(request: UpdateProfileRequest): UserProfile? =
      profileAction.update(request.profile)

  suspend fun deleteProfile(id: String): Boolean = profileAction.delete(id)

  suspend fun follow(followerId: String, followeeId: String): Boolean =
      followerAction.follow(followerId, followeeId)

  suspend fun unfollow(followerId: String, followeeId: String): Boolean =
      followerAction.unfollow(followerId, followeeId)

  suspend fun listFollowers(userId: String, page: Int, pageSize: Int): ListFollowersResponse {
    val ids = followerAction.listFollowers(userId).drop((page - 1) * pageSize).take(pageSize)
    return ListFollowersResponse(ids)
  }

  suspend fun listFollowing(userId: String, page: Int, pageSize: Int): ListFollowingResponse {
    val ids = followerAction.listFollowing(userId).drop((page - 1) * pageSize).take(pageSize)
    return ListFollowingResponse(ids)
  }
}
