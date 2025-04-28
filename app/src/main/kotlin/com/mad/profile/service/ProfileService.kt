package com.mad.profile.service

import com.mad.profile.actions.IFollowerAction
import com.mad.profile.actions.IProfileAction
import com.mad.profile.model.*
import java.util.UUID

/**
 * Реализация сервиса для работы с профилями пользователей и подписками.
 *
 * @property profileRepository репозиторий для работы с профилями пользователей
 * @property followerRepository репозиторий для работы с подписками пользователей
 */
class ProfileService(
    private val profileAction: IProfileAction,
    private val followerAction: IFollowerAction
) {
  suspend fun createProfile(req: ProfileRequest): ProfileResponse {
    val existing = profileAction.getByEmail(req.email)
    return existing ?: profileAction.createProfile(req)
  }

  suspend fun getProfile(id: UUID): ProfileResponse? = profileAction.getById(id)

  suspend fun listProfiles(page: Int, pageSize: Int): ProfilesResponse {
    val (list, total) = profileAction.list(page, pageSize)
    return ProfilesResponse(list, page, pageSize, total)
  }

  suspend fun updateProfile(id: UUID, req: ProfileRequest): ProfileResponse? =
      profileAction.update(id, req)

  suspend fun deleteProfile(id: UUID): Boolean = profileAction.delete(id)

  suspend fun follow(followerId: UUID, followeeId: UUID): Boolean =
      followerAction.follow(followerId, followeeId)

  suspend fun unfollow(followerId: UUID, followeeId: UUID): Boolean =
      followerAction.unfollow(followerId, followeeId)

  suspend fun listFollowers(userId: UUID, page: Int, pageSize: Int): FollowersResponse {
    val ids = followerAction.listFollowers(userId)
    val slice = ids.drop((page - 1) * pageSize).take(pageSize)
    return FollowersResponse(slice.map { it.toString() }, page, pageSize, ids.size)
  }

  suspend fun listFollowing(userId: UUID, page: Int, pageSize: Int): FollowingResponse {
    val ids = followerAction.listFollowing(userId)
    val slice = ids.drop((page - 1) * pageSize).take(pageSize)
    return FollowingResponse(slice.map { it.toString() }, page, pageSize, ids.size)
  }
}
