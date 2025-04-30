package com.mad.profile.actions

import com.mad.feed.dto.DbCreateRequest
import com.mad.feed.dto.DbDeleteRequest
import com.mad.feed.dto.DbReadRequest
import com.mad.feed.dto.DbResponse
import com.mad.profile.dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Класс для работы с подписками пользователей
 *
 * Содержит методы для подписки, отписки, получения списка подписчиков и подписок, а также проверки
 * на наличие подписки
 * @see IFollowerAction
 */
class FollowerAction(config: ApplicationConfig) : IFollowerAction {

  private val dbMode = config.propertyOrNull("ktor.database.mode")?.getString() ?: "LOCAL"
  private val dbHost = config.propertyOrNull("ktor.database.host")?.getString() ?: "localhost"
  private val dbPort = config.propertyOrNull("ktor.database.port")?.getString() ?: "8080"
  private val baseUrl =
      if (dbMode.equals("gateway", true)) "http://$dbHost:$dbPort/api/db"
      else "http://$dbHost:$dbPort"
  private val http = HttpClient { install(ContentNegotiation) { json() } }

  /**
   * Подписаться на пользователя
   *
   * @param followerId ID подписчика
   * @param followeeId ID подписки
   * @return true, если подписка успешна, false в противном случае
   */
  override suspend fun follow(followerId: String, followeeId: String): Boolean =
      withContext(Dispatchers.IO) {
        if (followerId == followeeId) return@withContext false
        if (isFollowing(followerId, followeeId)) return@withContext false

        val req =
            DbCreateRequest(
                table = "followers",
                data =
                    mapOf(
                        "follower_id" to followerId,
                        "followee_id" to followeeId,
                        "created_at" to Instant.now().toString()))
        val resp: DbResponse =
            http
                .post("$baseUrl/create") {
                  contentType(ContentType.Application.Json)
                  setBody(req)
                }
                .body()
        resp.success == true
      }

  /**
   * Отписаться от пользователя
   *
   * @param followerId ID подписчика
   * @param followeeId ID подписки
   * @return true, если отписка успешна, false в противном случае
   */
  override suspend fun unfollow(followerId: String, followeeId: String): Boolean =
      withContext(Dispatchers.IO) {
        val req =
            DbDeleteRequest(
                table = "followers",
                condition = "follower_id = ? AND followee_id = ?",
                conditionParams = listOf(followerId, followeeId))
        val resp: DbResponse =
            http
                .delete("$baseUrl/delete") {
                  contentType(ContentType.Application.Json)
                  setBody(req)
                }
                .body()
        resp.success == true
      }

  /**
   * Получить список подписчиков
   *
   * @param userId ID пользователя
   * @return Список ID подписчиков
   */
  override suspend fun listFollowers(userId: String): List<String> =
      readIds(mapOf("followee_id" to userId)) { it.follower_id }

  /**
   * Получить список подписок
   *
   * @param userId ID пользователя
   * @return Список ID подписок
   */
  override suspend fun listFollowing(userId: String): List<String> =
      readIds(mapOf("follower_id" to userId)) { it.followee_id }

  /**
   * Проверить, подписан ли пользователь на другого пользователя
   *
   * @param followerId ID подписчика
   * @param followeeId ID подписываемого пользователя
   * @return true, если подписан, false в противном случае
   */
  override suspend fun isFollowing(followerId: String, followeeId: String): Boolean =
      readIds(mapOf("follower_id" to followerId, "followee_id" to followeeId)) { it.follower_id }
          .isNotEmpty()

  /**
   * Вспомогательный метод для чтения ID из базы данных
   *
   * @param filters Фильтры для выборки
   * @param selector Функция для выбора нужного поля из строки базы данных
   * @return Список ID
   */
  private suspend fun readIds(
      filters: Map<String, String>,
      selector: (DbFollowerRow) -> String
  ): List<String> =
      http
          .post("$baseUrl/read") {
            contentType(ContentType.Application.Json)
            setBody(DbReadRequest(table = "followers", filters = filters))
          }
          .body<List<DbFollowerRow>>()
          .map(selector)
}
