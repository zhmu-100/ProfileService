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
 * - POST /profiles/{id}/follow - подписаться на пользователя
 * - POST /profiles/{id}/unfollow - отписаться от пользователя
 * - GET /profiles/{id}/followers - получить список подписчиков
 * - GET /profiles/{id}/following - получить список подписок
 *
 * Все маршруты включают валидацию входных данных и обработку ошибок.
 */
fun Routing.configureFollowerRoutes() {
  val profileService: ProfileService by inject()

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
     * Тело запроса - JSON с 2 id (follower_id и followee_id). В строке запроса так же указан id
     *
     * Возможные ответы:
     * - 204 No Content: отписка успешно выполнена
     * - 400 Bad Request: несоответствие ID
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
     * В строке запроса ID пользователя и параметры пагинации (page и page_size).
     *
     * Возможные ответы:
     * - 200 OK: список подписчиков
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
     * В строке запроса ID пользователя и параметры пагинации (page и page_size).
     *
     * Возможные ответы:
     * - 200 OK: список подписок
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
