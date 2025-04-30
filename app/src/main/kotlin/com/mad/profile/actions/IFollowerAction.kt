package com.mad.profile.actions

/**
 * Интерфейс для работы с подписками
 *
 * Возможные опции:
 * - Подписаться на пользователя
 * - Отписаться от пользователя
 * - Получить список подписчиков
 * - Получить список подписок
 * - Проверить, подписан ли пользователь на другого пользователя
 */
interface IFollowerAction {
  /**
   * Подписаться на пользователя
   *
   * @param followerId ID подписчика
   * @param followeeId ID подписываемого пользователя
   * @return true, если подписка успешна, false в противном случае
   */
  suspend fun follow(followerId: String, followeeId: String): Boolean

  /**
   * Отписаться от пользователя
   *
   * @param followerId ID подписчика
   * @param followeeId ID подписываемого пользователя
   * @return true, если отписка успешна, false в противном случае
   */
  suspend fun unfollow(followerId: String, followeeId: String): Boolean

  /**
   * Получить список подписчиков
   *
   * @param userId ID пользователя
   * @return Список ID подписчиков
   */
  suspend fun listFollowers(userId: String): List<String>

  /**
   * Получить список подписок
   *
   * @param userId ID пользователя
   * @return Список ID подписок
   */
  suspend fun listFollowing(userId: String): List<String>

  /**
   * Проверить, подписан ли пользователь на другого пользователя
   *
   * @param followerId ID подписчика
   * @param followeeId ID подписываемого пользователя
   * @return true, если подписан, false в противном случае
   */
  suspend fun isFollowing(followerId: String, followeeId: String): Boolean
}
