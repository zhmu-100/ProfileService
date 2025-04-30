package com.mad.profile.service

import com.mad.profile.actions.IFollowerAction
import com.mad.profile.actions.IProfileAction
import com.mad.profile.model.*

/**
 * Сервис для работы с профилями пользователей и подписками
 *
 * @property profileAction Действия с профилями
 * @property followerAction Действия с подписками
 */
class ProfileService(
    private val profileAction: IProfileAction,
    private val followerAction: IFollowerAction
) {
  /**
   * Создать профиль пользователя
   */
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

  /**
   * Получить профиль пользователя по ID
   */
  suspend fun getProfile(id: String): UserProfile? = profileAction.get(id)

  /**
   * Получить профиль пользователя по email
   */
  suspend fun listProfiles(page: Int, pageSize: Int): List<UserProfile> =
      profileAction.list(page, pageSize)

  /**
   * Получить профиль пользователя по email
   */
  suspend fun updateProfile(request: UpdateProfileRequest): UserProfile? =
      profileAction.update(request.profile)

  /**
   * Удалить профиль пользователя по ID
   */
  suspend fun deleteProfile(id: String): Boolean = profileAction.delete(id)

  /**
   * Получить количество подписчиков
   */
  suspend fun follow(followerId: String, followeeId: String): Boolean =
      followerAction.follow(followerId, followeeId)

  /**
   * Проверить, подписан ли пользователь на другого пользователя
   */
  suspend fun unfollow(followerId: String, followeeId: String): Boolean =
      followerAction.unfollow(followerId, followeeId)

  /**
   * Проверить, подписан ли пользователь на другого пользователя
   */
  suspend fun listFollowers(userId: String, page: Int, pageSize: Int): ListFollowersResponse {
    val ids = followerAction.listFollowers(userId).drop((page - 1) * pageSize).take(pageSize)
    return ListFollowersResponse(ids)
  }

  /**
   * Проверить, подписан ли пользователь на другого пользователя
   */
  suspend fun listFollowing(userId: String, page: Int, pageSize: Int): ListFollowingResponse {
    val ids = followerAction.listFollowing(userId).drop((page - 1) * pageSize).take(pageSize)
    return ListFollowingResponse(ids)
  }
}
