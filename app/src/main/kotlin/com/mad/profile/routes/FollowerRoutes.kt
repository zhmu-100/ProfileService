package com.mad.profile.routes

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
 * - POST /api/profiles/{id}/follow - подписаться на пользователя
 * - POST /api/profiles/{id}/unfollow - отписаться от пользователя
 * - GET /api/profiles/{id}/followers - получить список подписчиков
 * - GET /api/profiles/{id}/following - получить список подписок
 *
 * Все маршруты включают валидацию входных данных и обработку ошибок.
 */
fun Routing.configureFollowerRoutes() {
  val profileService: ProfileService by inject()

  route("/profiles/{id}") {

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
    post("/follow") {
      val id = call.parameters["id"]!!
      val body = call.receive<FollowRequest>()

      if (body.followee_id != id)
          return@post call.respond(
              HttpStatusCode.BadRequest,
              ErrorResponse("mismatch_id", "followee_id differs from path"))

      if (profileService.follow(body.follower_id, id)) call.respond(HttpStatusCode.NoContent)
      else call.respond(HttpStatusCode.BadRequest, ErrorResponse("follow_error", "Follow failed"))
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
    post("/unfollow") {
      val id = call.parameters["id"]!!
      val body = call.receive<UnfollowRequest>()

      if (body.followee_id != id)
          return@post call.respond(
              HttpStatusCode.BadRequest,
              ErrorResponse("mismatch_id", "followee_id differs from path"))

      if (profileService.unfollow(body.follower_id, id)) call.respond(HttpStatusCode.NoContent)
      else
          call.respond(
              HttpStatusCode.BadRequest, ErrorResponse("unfollow_error", "Unfollow failed"))
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
    get("/followers") {
      val id = call.parameters["id"]!!
      val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
      val pageSize = call.request.queryParameters["page_size"]?.toIntOrNull() ?: 10

      val resp = profileService.listFollowers(id, page, pageSize)
      call.respond(resp)
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
    get("/following") {
      val id = call.parameters["id"]!!
      val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
      val pageSize = call.request.queryParameters["page_size"]?.toIntOrNull() ?: 10

      val resp = profileService.listFollowing(id, page, pageSize)
      call.respond(resp)
    }
  }
}
