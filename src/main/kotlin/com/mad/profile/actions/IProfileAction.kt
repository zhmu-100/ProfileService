package com.mad.profile.actions

import com.mad.profile.model.UserProfile

/**
 * Интерфейс для работы с профилями пользователей
 *
 * Возможные опции:
 * - Создать профиль
 * - Получить профиль по ID
 * - Получить профиль по user_id
 * - Получить профиль по email
 * - Получить список профилей
 * - Обновить профиль
 * - Удалить профиль
 * - Получить количество подписчиков
 * - Получить количество подписок
 */
interface IProfileAction {
  /**
   * Создать профиль
   *
   * @param profile Профиль пользователя
   * @return Созданный профиль
   */
  suspend fun create(profile: UserProfile): UserProfile

  /**
   * Получить профиль по ID
   *
   * @param id ID профиля
   * @return Профиль пользователя или null, если не найден
   */
  suspend fun get(id: String): UserProfile?

  /**
   * Получить профиль по email
   *
   * @param email Email профиля
   * @return Профиль пользователя или null, если не найден
   */
  suspend fun getByEmail(email: String): UserProfile?

  /**
   * Получить список профилей
   *
   * @param page Номер страницы
   * @param pageSize Размер страницы
   * @return Список профилей
   */
  suspend fun list(page: Int, pageSize: Int): List<UserProfile>

  /**
   * Обновить профиль
   *
   * @param profile Профиль пользователя
   * @return Обновленный профиль или null, если не найден
   */
  suspend fun update(profile: UserProfile): UserProfile?

  /**
   * Удалить профиль
   *
   * @param id ID профиля
   * @return true, если удаление успешно, false в противном случае
   */
  suspend fun delete(id: String): Boolean

  /**
   * Получить количество подписчиков
   *
   * @param userId ID пользователя
   * @return Количество подписчиков
   */
  suspend fun followerCount(userId: String): Int

  /**
   * Получить количество подписок
   *
   * @param userId ID пользователя
   * @return Количество подписок
   */
  suspend fun followingCount(userId: String): Int

  /**
   * Получить профиль по user_id
   *
   * @param userId ID пользователя
   * @return Профиль пользователя или null, если не найден
   */
  suspend fun getByUserId(userId: String): UserProfile?
}
