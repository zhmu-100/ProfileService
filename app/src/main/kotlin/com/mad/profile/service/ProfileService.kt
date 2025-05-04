package com.mad.profile.service

import com.mad.profile.actions.IFollowerAction
import com.mad.profile.actions.IProfileAction
import com.mad.profile.logging.LoggerProvider
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
  private val logger = LoggerProvider.logger

  /** Создать профиль пользователя */
  suspend fun createProfile(request: CreateProfileRequest): UserProfile {
    logger.logActivity(
        "Создание профиля пользователя",
        additionalData = mapOf("email" to request.profile.email, "name" to request.profile.name))

    try {
      val candidate = request.profile

      // Проверяем, существует ли профиль с таким email
      profileAction.getByEmail(candidate.email)?.let {
        logger.logActivity(
            "Профиль с таким email уже существует",
            additionalData = mapOf("email" to candidate.email, "existingId" to it.id))
        return it
      }

      val withId =
          if (candidate.id.isBlank()) {
            val newId = java.util.UUID.randomUUID().toString()
            logger.logActivity(
                "Сгенерирован новый ID для профиля",
                additionalData = mapOf("email" to candidate.email, "newId" to newId))
            candidate.copy(id = newId)
          } else candidate

      val createdProfile = profileAction.create(withId)

      logger.logActivity(
          "Профиль пользователя успешно создан",
          additionalData =
              mapOf(
                  "id" to createdProfile.id,
                  "email" to createdProfile.email,
                  "name" to createdProfile.name))

      return createdProfile
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при создании профиля: email=${request.profile.email}",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }

  /** Получить профиль пользователя по ID */
  suspend fun getProfile(id: String): UserProfile? {
    logger.logActivity("Получение профиля по ID", additionalData = mapOf("id" to id))

    try {
      val profile = profileAction.get(id)

      if (profile == null) {
        logger.logActivity("Профиль не найден", additionalData = mapOf("id" to id))
      } else {
        logger.logActivity(
            "Профиль успешно получен",
            additionalData = mapOf("id" to id, "email" to profile.email, "name" to profile.name))
      }

      return profile
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при получении профиля: id=$id",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }

  /** Получить список профилей */
  suspend fun listProfiles(page: Int, pageSize: Int): List<UserProfile> {
    logger.logActivity(
        "Получение списка профилей",
        additionalData = mapOf("page" to page.toString(), "pageSize" to pageSize.toString()))

    try {
      val profiles = profileAction.list(page, pageSize)

      logger.logActivity(
          "Список профилей успешно получен",
          additionalData =
              mapOf(
                  "page" to page.toString(),
                  "pageSize" to pageSize.toString(),
                  "profilesCount" to profiles.size.toString()))

      return profiles
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при получении списка профилей: page=$page, pageSize=$pageSize",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }

  /** Обновить профиль пользователя */
  suspend fun updateProfile(request: UpdateProfileRequest): UserProfile? {
    logger.logActivity(
        "Обновление профиля пользователя",
        additionalData =
            mapOf(
                "id" to request.profile.id,
                "email" to request.profile.email,
                "name" to request.profile.name))

    try {
      val updatedProfile = profileAction.update(request.profile)

      if (updatedProfile == null) {
        logger.logActivity(
            "Профиль не найден при обновлении", additionalData = mapOf("id" to request.profile.id))
      } else {
        logger.logActivity(
            "Профиль успешно обновлен",
            additionalData =
                mapOf(
                    "id" to updatedProfile.id,
                    "email" to updatedProfile.email,
                    "name" to updatedProfile.name))
      }

      return updatedProfile
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при обновлении профиля: id=${request.profile.id}",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }

  /** Удалить профиль пользователя по ID */
  suspend fun deleteProfile(id: String): Boolean {
    logger.logActivity("Удаление профиля пользователя", additionalData = mapOf("id" to id))

    try {
      val success = profileAction.delete(id)

      if (success) {
        logger.logActivity("Профиль успешно удален", additionalData = mapOf("id" to id))
      } else {
        logger.logActivity("Профиль не найден при удалении", additionalData = mapOf("id" to id))
      }

      return success
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при удалении профиля: id=$id",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }

  /** Подписаться на пользователя */
  suspend fun follow(followerId: String, followeeId: String): Boolean {
    logger.logActivity(
        "Подписка на пользователя",
        additionalData = mapOf("followerId" to followerId, "followeeId" to followeeId))

    try {
      val success = followerAction.follow(followerId, followeeId)

      if (success) {
        logger.logActivity(
            "Подписка успешно выполнена",
            additionalData = mapOf("followerId" to followerId, "followeeId" to followeeId))
      } else {
        logger.logActivity(
            "Ошибка при подписке",
            additionalData = mapOf("followerId" to followerId, "followeeId" to followeeId))
      }

      return success
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при подписке: followerId=$followerId, followeeId=$followeeId",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }

  /** Отписаться от пользователя */
  suspend fun unfollow(followerId: String, followeeId: String): Boolean {
    logger.logActivity(
        "Отписка от пользователя",
        additionalData = mapOf("followerId" to followerId, "followeeId" to followeeId))

    try {
      val success = followerAction.unfollow(followerId, followeeId)

      if (success) {
        logger.logActivity(
            "Отписка успешно выполнена",
            additionalData = mapOf("followerId" to followerId, "followeeId" to followeeId))
      } else {
        logger.logActivity(
            "Ошибка при отписке",
            additionalData = mapOf("followerId" to followerId, "followeeId" to followeeId))
      }

      return success
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при отписке: followerId=$followerId, followeeId=$followeeId",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }

  /** Получить список подписчиков */
  suspend fun listFollowers(userId: String, page: Int, pageSize: Int): ListFollowersResponse {
    logger.logActivity(
        "Получение списка подписчиков",
        additionalData =
            mapOf("userId" to userId, "page" to page.toString(), "pageSize" to pageSize.toString()))

    try {
      val ids = followerAction.listFollowers(userId).drop((page - 1) * pageSize).take(pageSize)

      logger.logActivity(
          "Список подписчиков успешно получен",
          additionalData = mapOf("userId" to userId, "followersCount" to ids.size.toString()))

      return ListFollowersResponse(ids)
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при получении списка подписчиков: userId=$userId, page=$page, pageSize=$pageSize",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }

  /** Получить список подписок */
  suspend fun listFollowing(userId: String, page: Int, pageSize: Int): ListFollowingResponse {
    logger.logActivity(
        "Получение списка подписок",
        additionalData =
            mapOf("userId" to userId, "page" to page.toString(), "pageSize" to pageSize.toString()))

    try {
      val ids = followerAction.listFollowing(userId).drop((page - 1) * pageSize).take(pageSize)

      logger.logActivity(
          "Список подписок успешно получен",
          additionalData = mapOf("userId" to userId, "followingCount" to ids.size.toString()))

      return ListFollowingResponse(ids)
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при получении списка подписок: userId=$userId, page=$page, pageSize=$pageSize",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }
}
