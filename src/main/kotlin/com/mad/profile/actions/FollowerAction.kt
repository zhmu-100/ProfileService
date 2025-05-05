package com.mad.profile.actions

import com.mad.feed.dto.DbCreateRequest
import com.mad.feed.dto.DbDeleteRequest
import com.mad.feed.dto.DbReadRequest
import com.mad.feed.dto.DbResponse
import com.mad.profile.dto.*
import com.mad.profile.logging.LoggerProvider
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
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
class FollowerAction : IFollowerAction {
  private val logger = LoggerProvider.logger
  private val dotenv = dotenv()
  private val dbMode = dotenv["DB_MODE"] ?: "LOCAL"
  private val dbHost = dotenv["DB_HOST"] ?: "localhost"
  private val dbPort = dotenv["DB_PORT"] ?: "8080"
  private val baseUrl =
      if (dbMode.equals("gateway", true)) {
        "http://$dbHost:$dbPort/api/db"
      } else {
        "http://$dbHost:$dbPort"
      }
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
        logger.logActivity(
            "Подписка на пользователя",
        )

        if (followerId == followeeId) {
          logger.logActivity(
              "Попытка подписаться на самого себя",
          )
          return@withContext false
        }

        if (isFollowing(followerId, followeeId)) {
          logger.logActivity(
              "Пользователь уже подписан",
          )
          return@withContext false
        }

        try {
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

          val success = resp.success == true

          if (success) {
            logger.logActivity(
                "Подписка успешно создана",
            )
          } else {
            logger.logError(
                "Ошибка при создании подписки",
                errorMessage = resp.error ?: "Неизвестная ошибка",
            )
          }

          success
        } catch (e: Exception) {
          logger.logError(
              "Исключение при создании подписки: followerId=$followerId, followeeId=$followeeId",
              errorMessage = e.message ?: "Неизвестная ошибка",
              stackTrace = e.stackTraceToString())
          throw e
        }
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
        logger.logActivity(
            "Отписка от пользователя",
        )

        try {
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

          val success = resp.success == true

          if (success) {
            logger.logActivity(
                "Отписка выполнена успешно",
            )
          } else {
            logger.logActivity(
                "Отписка не выполнена (возможно, подписки не существовало)",
            )
          }

          success
        } catch (e: Exception) {
          logger.logError(
              "Исключение при отписке: followerId=$followerId, followeeId=$followeeId",
              errorMessage = e.message ?: "Неизвестная ошибка",
              stackTrace = e.stackTraceToString())
          throw e
        }
      }

  /**
   * Получить список подписчиков
   *
   * @param userId ID пользователя
   * @return Список ID подписчиков
   */
  override suspend fun listFollowers(userId: String): List<String> {
    logger.logActivity(
        "Получение списка подписчиков",
    )

    try {
      val followers = readIds(mapOf("followee_id" to userId)) { it.follower_id }

      logger.logActivity(
          "Список подписчиков получен успешно",
      )

      return followers
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при получении списка подписчиков: userId=$userId",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }

  /**
   * Получить список подписок
   *
   * @param userId ID пользователя
   * @return Список ID подписок
   */
  override suspend fun listFollowing(userId: String): List<String> {
    logger.logActivity(
        "Получение списка подписок",
    )

    try {
      val following = readIds(mapOf("follower_id" to userId)) { it.followee_id }

      logger.logActivity(
          "Список подписок получен успешно",
      )

      return following
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при получении списка подписок: userId=$userId",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }

  /**
   * Проверить, подписан ли пользователь на другого пользователя
   *
   * @param followerId ID подписчика
   * @param followeeId ID подписываемого пользователя
   * @return true, если подписан, false в противном случае
   */
  override suspend fun isFollowing(followerId: String, followeeId: String): Boolean {
    logger.logActivity(
        "Проверка наличия подписки",
    )

    try {
      val result =
          readIds(mapOf("follower_id" to followerId, "followee_id" to followeeId)) {
                it.follower_id
              }
              .isNotEmpty()

      logger.logActivity(
          "Результат проверки подписки",
      )

      return result
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при проверке подписки: followerId=$followerId, followeeId=$followeeId",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }

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
  ): List<String> {
    logger.logActivity(
        "Запрос к БД: чтение данных подписок",
    )

    try {
      val result =
          http
              .post("$baseUrl/read") {
                contentType(ContentType.Application.Json)
                setBody(DbReadRequest(table = "followers", filters = filters))
              }
              .body<List<DbFollowerRow>>()
              .map(selector)

      logger.logActivity(
          "Данные подписок получены успешно",
      )

      return result
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при чтении данных подписок: filters=$filters",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }
}
