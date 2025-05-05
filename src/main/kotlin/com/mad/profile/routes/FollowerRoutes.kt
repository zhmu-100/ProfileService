package com.mad.profile.routes

import com.mad.profile.logging.LoggerProvider
import com.mad.profile.model.ErrorResponse
import com.mad.profile.model.FollowRequest
import com.mad.profile.model.UnfollowRequest
import com.mad.profile.service.ProfileService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*
import org.koin.ktor.ext.inject

/**
 * Конфигурация маршрутов для работы с подписками пользователей.
 *
 * Регистрирует следующие endpoints:
 * - POST /profiles/{id}/follow - подписаться на пользователя
 * - POST /profiles/{id}/unfollow - отписаться от пользователя
 * - GET /profiles/{id}/followers - получить список подписчиков
 * - GET /profiles/{id}/following - получить список подписок
 *
 * Все маршруты включают валидацию входных данных и обработку ошибок.
 */
fun Routing.configureFollowerRoutes() {
  val profileService: ProfileService by inject()
  val logger = LoggerProvider.logger

  route("/profiles/{id}") {

    /**
     * Подписаться на пользователя.
     *
     * Тело запроса - JSON с 2 id (follower_id и followee_id). В строке запроса так же указан id
     *
     * Возможные ответы
     * - 204 No Content: подписка успешно выполнена
     * - 400 Bad Request: несоответствие ID
     */
    post("/follow") {
      val id = call.parameters["id"]!!

      logger.logActivity(
          "API: Запрос на подписку на пользователя", additionalData = mapOf("followeeId" to id))

      try {
        val body = call.receive<FollowRequest>()

        logger.logActivity(
            "API: Получены данные для подписки",
            additionalData =
                mapOf("followerId" to body.follower_id, "followeeId" to body.followee_id))

        if (body.followee_id != id) {
          logger.logActivity(
              "API: Несоответствие ID при подписке",
              additionalData = mapOf("pathId" to id, "bodyFolloweeId" to body.followee_id))
          call.respond(
              HttpStatusCode.BadRequest,
              ErrorResponse("mismatch_id", "followee_id differs from path"))
          return@post
        }

        val success = profileService.follow(body.follower_id, id)

        if (success) {
          logger.logActivity(
              "API: Подписка успешно выполнена",
              additionalData = mapOf("followerId" to body.follower_id, "followeeId" to id))
          call.respond(HttpStatusCode.NoContent)
        } else {
          logger.logActivity(
              "API: Ошибка при подписке",
              additionalData = mapOf("followerId" to body.follower_id, "followeeId" to id))
          call.respond(HttpStatusCode.BadRequest, ErrorResponse("follow_error", "Follow failed"))
        }
      } catch (e: Exception) {
        logger.logError(
            "API: Исключение при подписке на пользователя: followeeId=$id",
            errorMessage = e.message ?: "Неизвестная ошибка",
            stackTrace = e.stackTraceToString())
        throw e
      }
    }

    /**
     * Отписаться от пользователя.
     *
     * Тело запроса - JSON с 2 id (follower_id и followee_id). В строке запроса так же указан id
     *
     * Возможные ответы:
     * - 204 No Content: отписка успешно выполнена
     * - 400 Bad Request: несоответствие ID
     */
    post("/unfollow") {
      val id = call.parameters["id"]!!

      logger.logActivity(
          "API: Запрос на отписку от пользователя", additionalData = mapOf("followeeId" to id))

      try {
        val body = call.receive<UnfollowRequest>()

        logger.logActivity(
            "API: Получены данные для отписки",
            additionalData =
                mapOf("followerId" to body.follower_id, "followeeId" to body.followee_id))

        if (body.followee_id != id) {
          logger.logActivity(
              "API: Несоответствие ID при отписке",
              additionalData = mapOf("pathId" to id, "bodyFolloweeId" to body.followee_id))
          call.respond(
              HttpStatusCode.BadRequest,
              ErrorResponse("mismatch_id", "followee_id differs from path"))
          return@post
        }

        val success = profileService.unfollow(body.follower_id, id)

        if (success) {
          logger.logActivity(
              "API: Отписка успешно выполнена",
              additionalData = mapOf("followerId" to body.follower_id, "followeeId" to id))
          call.respond(HttpStatusCode.NoContent)
        } else {
          logger.logActivity(
              "API: Ошибка при отписке",
              additionalData = mapOf("followerId" to body.follower_id, "followeeId" to id))
          call.respond(
              HttpStatusCode.BadRequest, ErrorResponse("unfollow_error", "Unfollow failed"))
        }
      } catch (e: Exception) {
        logger.logError(
            "API: Исключение при отписке от пользователя: followeeId=$id",
            errorMessage = e.message ?: "Неизвестная ошибка",
            stackTrace = e.stackTraceToString())
        throw e
      }
    }

    /**
     * Получить список подписчиков пользователя.
     *
     * В строке запроса ID пользователя и параметры пагинации (page и page_size).
     *
     * Возможные ответы:
     * - 200 OK: список подписчиков
     */
    get("/followers") {
      val id = call.parameters["id"]!!
      val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
      val pageSize = call.request.queryParameters["page_size"]?.toIntOrNull() ?: 10

      logger.logActivity(
          "API: Запрос на получение списка подписчиков",
          additionalData =
              mapOf("userId" to id, "page" to page.toString(), "pageSize" to pageSize.toString()))

      try {
        val resp = profileService.listFollowers(id, page, pageSize)

        logger.logActivity(
            "API: Список подписчиков успешно получен",
        )

        call.respond(resp)
      } catch (e: Exception) {
        logger.logError(
            "API: Исключение при получении списка подписчиков: userId=$id, page=$page, pageSize=$pageSize",
            errorMessage = e.message ?: "Неизвестная ошибка",
            stackTrace = e.stackTraceToString())
        throw e
      }
    }

    /**
     * Получить список подписок пользователя.
     *
     * В строке запроса ID пользователя и параметры пагинации (page и page_size).
     *
     * Возможные ответы:
     * - 200 OK: список подписок
     */
    get("/following") {
      val id = call.parameters["id"]!!
      val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
      val pageSize = call.request.queryParameters["page_size"]?.toIntOrNull() ?: 10

      logger.logActivity(
          "API: Запрос на получение списка подписок",
          additionalData =
              mapOf("userId" to id, "page" to page.toString(), "pageSize" to pageSize.toString()))

      try {
        val resp = profileService.listFollowing(id, page, pageSize)

        logger.logActivity("API: Список подписок успешно получен")

        call.respond(resp)
      } catch (e: Exception) {
        logger.logError(
            "API: Исключение при получении списка подписок: userId=$id, page=$page, pageSize=$pageSize",
            errorMessage = e.message ?: "Неизвестная ошибка",
            stackTrace = e.stackTraceToString())
        throw e
      }
    }
  }
}
