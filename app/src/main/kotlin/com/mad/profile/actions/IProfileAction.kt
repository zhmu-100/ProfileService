package com.mad.profile.actions

import com.mad.profile.model.UserProfile

/**
 * Интерфейс для работы с профилями пользователей
 *
 * Возможные опции:
 * - Создать профиль
 * - Получить профиль по ID
 * - Получить профиль по email
 * - Получить список профилей
 * - Обновить профиль
 * - Удалить профиль
 * - Получить количество подписчиков
 * - Получить количество подписок
 */
interface IProfileAction {
  suspend fun create(profile: UserProfile): UserProfile
  suspend fun get(id: String): UserProfile?
  suspend fun getByEmail(email: String): UserProfile?
  suspend fun list(page: Int, pageSize: Int): List<UserProfile>
  suspend fun update(profile: UserProfile): UserProfile?
  suspend fun delete(id: String): Boolean
  suspend fun followerCount(userId: String): Int
  suspend fun followingCount(userId: String): Int
}
