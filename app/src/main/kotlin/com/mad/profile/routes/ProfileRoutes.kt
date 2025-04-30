package com.mad.profile.routes

import com.mad.profile.model.*
import com.mad.profile.service.ProfileService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

/**
 * Конфигурация маршрутов для работы с профилями пользователей.
 *
 * Регистрирует следующие endpoints:
 * - POST /api/profiles - создание профиля
 * - GET /api/profiles/{id} - получение профиля по ID
 * - PUT /api/profiles/{id} - обновление профиля
 * - DELETE /api/profiles/{id} - удаление профиля
 * - GET /api/profiles - список профилей с пагинацией
 *
 * Все маршруты обрабатывают валидацию входных данных и возвращают соответствующие HTTP статусы.
 */
fun Routing.configureProfileRoutes() {
  val profileService: ProfileService by inject()

  route("/api/profiles") {

    /**
     * Создание нового профиля пользователя.
     *
     * Параметры (в теле запроса):
     * - name: String (обязательное, не пустое)
     * - email: String (обязательное, валидный формат email)
     *
     * Возможные ответы:
     * - 201 Created: профиль успешно создан
     * - 400 Bad Request: ошибки валидации
     * - 500 Internal Server Error: серверная ошибка
     */
    post {
      val profileRequest = call.receive<CreateProfileRequest>()

      if (profileRequest.profile.name.isBlank()) {
        return@post call.respond(
            HttpStatusCode.BadRequest, ErrorResponse("validation_error", "Name cannot be empty"))
      }

      if (!isValidEmail(profileRequest.profile.email)) {
        return@post call.respond(
            HttpStatusCode.BadRequest, ErrorResponse("validation_error", "Invalid email format"))
      }

      val created = profileService.createProfile(profileRequest)
      call.respond(HttpStatusCode.Created, created)
    }

    /**
     * Получение профиля по ID.
     *
     * Параметры:
     * - id: UUID (в пути запроса)
     *
     * Возможные ответы:
     * - 200 OK: профиль найден
     * - 400 Bad Request: неверный формат UUID
     * - 404 Not Found: профиль не найден
     */
    get("/{id}") {
      val id =
          call.parameters["id"]
              ?: return@get call.respond(
                  HttpStatusCode.BadRequest,
                  ErrorResponse("invalid_id", "Missing id"),
              )

      val profile = profileService.getProfile(id)
      if (profile == null) {
        call.respond(HttpStatusCode.NotFound, ErrorResponse("not_found", "Profile not found"))
      } else {
        call.respond(profile)
      }
    }

    /**
     * Обновление профиля.
     *
     * Параметры:
     * - id: UUID (в пути запроса)
     * - name: String (в теле запроса)
     * - email: String (в теле запроса)
     *
     * Возможные ответы:
     * - 200 OK: профиль успешно обновлен
     * - 400 Bad Request: неверный формат UUID
     * - 404 Not Found: профиль не найден
     */
    put("/{id}") {
      val id =
          call.parameters["id"]
              ?: return@put call.respond(
                  HttpStatusCode.BadRequest, ErrorResponse("invalid_id", "Missing profile id"))

      val req = call.receive<UpdateProfileRequest>()

      if (req.profile.id != id) {
        return@put call.respond(
            HttpStatusCode.BadRequest, ErrorResponse("mismatch_id", "ID in path and body differ"))
      }

      val updated = profileService.updateProfile(req)
      if (updated == null) {
        call.respond(HttpStatusCode.NotFound, ErrorResponse("not_found", "Profile not found"))
      } else {
        call.respond(updated)
      }
    }

    /**
     * Удаление профиля.
     *
     * Параметры:
     * - id: UUID (в пути запроса)
     *
     * Возможные ответы:
     * - 204 No Content: профиль успешно удален
     * - 400 Bad Request: неверный формат UUID
     * - 404 Not Found: профиль не найден
     */
    delete("/{id}") {
      val id = call.parameters["id"] ?: return@delete
      call.respond(HttpStatusCode.BadRequest, ErrorResponse("invalid_id", "Missing profile id"))

      if (profileService.deleteProfile(id)) call.respond(HttpStatusCode.NoContent)
      else call.respond(HttpStatusCode.NotFound, ErrorResponse("not_found", "Profile not found"))
    }

    /**
     * Получение списка профилей с пагинацией.
     *
     * Параметры запроса:
     * - page: Int (по умолчанию 1)
     * - pageSize: Int (по умолчанию 10, максимум 100)
     *
     * Возможные ответы:
     * - 200 OK: список профилей
     * - 400 Bad Request: неверные параметры пагинации
     */
    get {
      val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
      val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 10

      if (page < 1 || pageSize < 1 || pageSize > 100) {
        return@get call.respond(
            HttpStatusCode.BadRequest,
            ErrorResponse("invalid_pagination", "Invalid pagination parameters"))
      }

      val result = profileService.listProfiles(page, pageSize)
      call.respond(result)
    }
  }
}

/**
 * Проверяет валидность email адреса.
 *
 * @param email Строка для проверки
 * @return true если email соответствует формату, иначе false
 */
private fun isValidEmail(email: String): Boolean {
  val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
  return email.matches(emailRegex.toRegex())
}
