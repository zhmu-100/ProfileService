package com.mad.profile.routes

import com.mad.profile.model.ErrorResponse
import com.mad.profile.model.FollowRequest
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
 * - POST /api/profiles/{id}/follow - подписаться на пользователя
 * - POST /api/profiles/{id}/unfollow - отписаться от пользователя
 * - GET /api/profiles/{id}/followers - получить список подписчиков
 * - GET /api/profiles/{id}/following - получить список подписок
 *
 * Все маршруты включают валидацию входных данных и обработку ошибок.
 */
fun Routing.configureFollowerRoutes() {
  val profileService: ProfileService by inject()

  route("/api/profiles") {

    /**
     * Подписаться на пользователя.
     *
     * @param id UUID пользователя, на которого подписываются (в пути)
     * @param followerId UUID подписчика (в теле запроса)
     *
     * Возможные ответы:
     * - 204 No Content: подписка успешно оформлена
     * - 400 Bad Request: неверный формат UUID или ошибка подписки
     * - 500 Internal Server Error: серверная ошибка
     */
    post("/{id}/follow") {
      try {
        val followeeId =
            call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }

        if (followeeId == null) {
          call.respond(
              HttpStatusCode.BadRequest, ErrorResponse("invalid_id", "Invalid profile ID format"))
          return@post
        }

        val followRequest = call.receive<FollowRequest>()
        val followerId = runCatching { UUID.fromString(followRequest.followerId) }.getOrNull()

        if (followerId == null) {
          call.respond(
              HttpStatusCode.BadRequest, ErrorResponse("invalid_id", "Invalid follower ID format"))
          return@post
        }

        val success = profileService.follow(followerId, followeeId)
        if (success) {
          call.respond(HttpStatusCode.NoContent)
        } else {
          call.respond(
              HttpStatusCode.BadRequest, ErrorResponse("follow_error", "Failed to follow user"))
        }
      } catch (e: Exception) {
        call.respond(
            HttpStatusCode.InternalServerError,
            ErrorResponse("server_error", "Failed to follow user"))
      }
    }

    /**
     * Отписаться от пользователя.
     *
     * @param id UUID пользователя, от которого отписываются (в пути)
     * @param followerId UUID отписывающегося (в теле запроса)
     * @param followeeId UUID пользователя, от которого отписываются (в теле запроса)
     *
     * Особенности:
     * - Проверяет соответствие followeeId из пути и тела запроса
     *
     * Возможные ответы:
     * - 204 No Content: отписка успешно выполнена
     * - 400 Bad Request: неверный формат UUID или несоответствие ID
     * - 500 Internal Server Error: серверная ошибка
     */
    post("/{id}/unfollow") {
      try {
        val followeeIdFromUrl =
            call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }

        if (followeeIdFromUrl == null) {
          call.respond(
              HttpStatusCode.BadRequest, ErrorResponse("invalid_id", "Invalid profile ID format"))
          return@post
        }

        val unfollowRequest = call.receive<FollowRequest>()
        val followerId = runCatching { UUID.fromString(unfollowRequest.followerId) }.getOrNull()
        val followeeIdFromBody =
            runCatching { UUID.fromString(unfollowRequest.followeeId) }.getOrNull()

        if (followerId == null || followeeIdFromBody == null) {
          call.respond(
              HttpStatusCode.BadRequest, ErrorResponse("invalid_id", "Invalid UUID format in body"))
          return@post
        }

        // Вот эта проверка ключевая
        if (followeeIdFromUrl != followeeIdFromBody) {
          call.respond(
              HttpStatusCode.BadRequest,
              ErrorResponse("mismatch_id", "Followee ID in path and body do not match"))
          return@post
        }

        val success = profileService.unfollow(followerId, followeeIdFromUrl)

        if (success) {
          call.respond(HttpStatusCode.NoContent)
        } else {
          call.respond(
              HttpStatusCode.BadRequest, ErrorResponse("unfollow_error", "Failed to unfollow user"))
        }
      } catch (e: Exception) {
        call.respond(
            HttpStatusCode.InternalServerError,
            ErrorResponse("server_error", "Failed to unfollow user"))
      }
    }

    /**
     * Получить список подписчиков пользователя.
     *
     * @param id UUID пользователя
     * @param page Номер страницы (по умолчанию 1)
     * @param pageSize Размер страницы (по умолчанию 10, максимум 100)
     *
     * Возможные ответы:
     * - 200 OK: список подписчиков
     * - 400 Bad Request: неверный формат UUID или параметров пагинации
     * - 500 Internal Server Error: серверная ошибка
     */
    get("/{id}/followers") {
      try {
        val userId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }

        if (userId == null) {
          call.respond(
              HttpStatusCode.BadRequest, ErrorResponse("invalid_id", "Invalid profile ID format"))
          return@get
        }

        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 10

        if (page < 1 || pageSize < 1 || pageSize > 100) {
          call.respond(
              HttpStatusCode.BadRequest,
              ErrorResponse("invalid_pagination", "Invalid pagination parameters"))
          return@get
        }

        val followers = profileService.listFollowers(userId, page, pageSize)
        call.respond(HttpStatusCode.OK, followers)
      } catch (e: Exception) {
        call.respond(
            HttpStatusCode.InternalServerError,
            ErrorResponse("server_error", "Failed to list followers"))
      }
    }

    /**
     * Получить список подписок пользователя.
     *
     * @param id UUID пользователя
     * @param page Номер страницы (по умолчанию 1)
     * @param pageSize Размер страницы (по умолчанию 10, максимум 100)
     *
     * Возможные ответы:
     * - 200 OK: список подписок
     * - 400 Bad Request: неверный формат UUID или параметров пагинации
     * - 500 Internal Server Error: серверная ошибка
     */
    get("/{id}/following") {
      try {
        val userId = call.parameters["id"]?.let { runCatching { UUID.fromString(it) }.getOrNull() }

        if (userId == null) {
          call.respond(
              HttpStatusCode.BadRequest, ErrorResponse("invalid_id", "Invalid profile ID format"))
          return@get
        }

        val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
        val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 10

        if (page < 1 || pageSize < 1 || pageSize > 100) {
          call.respond(
              HttpStatusCode.BadRequest,
              ErrorResponse("invalid_pagination", "Invalid pagination parameters"))
          return@get
        }

        val following = profileService.listFollowing(userId, page, pageSize)
        call.respond(HttpStatusCode.OK, following)
      } catch (e: Exception) {
        call.respond(
            HttpStatusCode.InternalServerError,
            ErrorResponse("server_error", "Failed to list following"))
      }
    }
  }
}
