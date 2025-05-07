package com.mad.profile.routes

import com.mad.profile.actions.IProfileAction
import com.mad.profile.logging.LoggerProvider
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
 * - POST /profiles - создание профиля
 * - GET /profiles/{id} - получение профиля по ID
 * - GET /profiles/user/{user_id} - получение профиля по user_id
 * - PUT /profiles/{id} - обновление профиля
 * - DELETE /profiles/{id} - удаление профиля
 * - GET /profiles - список профилей с пагинацией
 *
 * Все маршруты обрабатывают валидацию входных данных и возвращают соответствующие HTTP статусы.
 */
fun Routing.configureProfileRoutes() {
  val profileService: ProfileService by inject()
  val profileAction: IProfileAction by inject()
  val logger = LoggerProvider.logger

  route("/profiles") {

    /**
     * Создание нового профиля пользователя.
     *
     * В теле запроса - профиль в формате JSON.
     */
    post {
      logger.logActivity("API: Запрос на создание профиля")

      try {
        val profileRequest = call.receive<CreateProfileRequest>()

        logger.logActivity(
            "API: Получены данные профиля",
            additionalData =
                mapOf(
                    "name" to profileRequest.profile.name, "email" to profileRequest.profile.email))

        if (profileRequest.profile.name.isBlank()) {
          logger.logActivity(
              "API: Ошибка валидации - пустое имя",
              additionalData = mapOf("email" to profileRequest.profile.email))
          call.respond(
              HttpStatusCode.BadRequest, ErrorResponse("validation_error", "Name cannot be empty"))
          return@post
        }

        if (!isValidEmail(profileRequest.profile.email)) {
          logger.logActivity(
              "API: Ошибка валидации - некорректный email",
              additionalData = mapOf("email" to profileRequest.profile.email))
          call.respond(
              HttpStatusCode.BadRequest, ErrorResponse("validation_error", "Invalid email format"))
          return@post
        }

        val created = profileService.createProfile(profileRequest)

        logger.logActivity(
            "API: Профиль успешно создан",
            additionalData =
                mapOf("id" to created.id, "email" to created.email, "name" to created.name))

        call.respond(HttpStatusCode.Created, created)
      } catch (e: Exception) {
        logger.logError(
            "API: Исключение при создании профиля",
            errorMessage = e.message ?: "Неизвестная ошибка",
            stackTrace = e.stackTraceToString())
        throw e
      }
    }

    /**
     * Получение профиля по ID.
     *
     * В url ид профиля
     */
    get("/{id}") {
      val id = call.parameters["id"]

      if (id == null) {
        logger.logActivity("API: Ошибка запроса на получение профиля - отсутствует ID")
        call.respond(
            HttpStatusCode.BadRequest,
            ErrorResponse("invalid_id", "Missing id"),
        )
        return@get
      }

      logger.logActivity("API: Запрос на получение профиля", additionalData = mapOf("id" to id))

      try {
        val profile = profileService.getProfile(id)

        if (profile == null) {
          logger.logActivity("API: Профиль не найден", additionalData = mapOf("id" to id))
          call.respond(HttpStatusCode.NotFound, ErrorResponse("not_found", "Profile not found"))
        } else {
          logger.logActivity(
              "API: Профиль успешно получен",
              additionalData = mapOf("id" to id, "email" to profile.email, "name" to profile.name))
          call.respond(profile)
        }
      } catch (e: Exception) {
        logger.logError(
            "API: Исключение при получении профиля: id=$id",
            errorMessage = e.message ?: "Неизвестная ошибка",
            stackTrace = e.stackTraceToString())
        throw e
      }
    }

    /**
     * Получение профиля по user_id.
     *
     * В URL user_id пользователя
     */
    get("/user/{user_id}") {
      val userId = call.parameters["user_id"]

      if (userId == null) {
        logger.logActivity("API: Ошибка запроса на получение профиля - отсутствует user_id")
        call.respond(
            HttpStatusCode.BadRequest,
            ErrorResponse("invalid_user_id", "Missing user_id"),
        )
        return@get
      }

      logger.logActivity(
          "API: Запрос на получение профиля по user_id",
          additionalData = mapOf("user_id" to userId))

      try {
        val profile = profileAction.getByUserId(userId)

        if (profile == null) {
          logger.logActivity(
              "API: Профиль не найден по user_id", additionalData = mapOf("user_id" to userId))
          call.respond(HttpStatusCode.NotFound, ErrorResponse("not_found", "Profile not found"))
        } else {
          logger.logActivity(
              "API: Профиль успешно получен по user_id",
              additionalData =
                  mapOf(
                      "id" to profile.id,
                      "user_id" to userId,
                      "email" to profile.email,
                      "name" to profile.name))
          call.respond(profile)
        }
      } catch (e: Exception) {
        logger.logError(
            "API: Исключение при получении профиля по user_id: user_id=$userId",
            errorMessage = e.message ?: "Неизвестная ошибка",
            stackTrace = e.stackTraceToString())
        throw e
      }
    }

    /**
     * Обновление профиля.
     *
     * В теле - JSON с обновленным профилем.
     */
    put("/{id}") {
      val id = call.parameters["id"]

      if (id == null) {
        logger.logActivity("API: Ошибка запроса на обновление профиля - отсутствует ID")
        call.respond(HttpStatusCode.BadRequest, ErrorResponse("invalid_id", "Missing profile id"))
        return@put
      }

      logger.logActivity("API: Запрос на обновление профиля", additionalData = mapOf("id" to id))

      try {
        val req = call.receive<UpdateProfileRequest>()

        logger.logActivity(
            "API: Получены данные для обновления профиля",
            additionalData =
                mapOf(
                    "id" to id,
                    "profileId" to req.profile.id,
                    "email" to req.profile.email,
                    "name" to req.profile.name))

        if (req.profile.id != id) {
          logger.logActivity(
              "API: Несоответствие ID при обновлении профиля",
              additionalData = mapOf("pathId" to id, "bodyId" to req.profile.id))
          call.respond(
              HttpStatusCode.BadRequest, ErrorResponse("mismatch_id", "ID in path and body differ"))
          return@put
        }

        val updated = profileService.updateProfile(req)

        if (updated == null) {
          logger.logActivity(
              "API: Профиль не найден при обновлении", additionalData = mapOf("id" to id))
          call.respond(HttpStatusCode.NotFound, ErrorResponse("not_found", "Profile not found"))
        } else {
          logger.logActivity(
              "API: Профиль успешно обновлен",
              additionalData = mapOf("id" to id, "email" to updated.email, "name" to updated.name))
          call.respond(updated)
        }
      } catch (e: Exception) {
        logger.logError(
            "API: Исключение при обновлении профиля: id=$id",
            errorMessage = e.message ?: "Неизвестная ошибка",
            stackTrace = e.stackTraceToString())
        throw e
      }
    }

    /**
     * Удаление профиля.
     *
     * В пути запроса id профиля
     */
    delete("/{id}") {
      val id = call.parameters["id"]

      if (id == null) {
        logger.logActivity("API: Ошибка запроса на удаление профиля - отсутствует ID")
        call.respond(HttpStatusCode.BadRequest, ErrorResponse("invalid_id", "Missing profile id"))
        return@delete
      }

      logger.logActivity("API: Запрос на удаление профиля", additionalData = mapOf("id" to id))

      try {
        val success = profileService.deleteProfile(id)

        if (success) {
          logger.logActivity("API: Профиль успешно удален", additionalData = mapOf("id" to id))
          call.respond(HttpStatusCode.NoContent)
        } else {
          logger.logActivity(
              "API: Профиль не найден при удалении", additionalData = mapOf("id" to id))
          call.respond(HttpStatusCode.NotFound, ErrorResponse("not_found", "Profile not found"))
        }
      } catch (e: Exception) {
        logger.logError(
            "API: Исключение при удалении профиля: id=$id",
            errorMessage = e.message ?: "Неизвестная ошибка",
            stackTrace = e.stackTraceToString())
        throw e
      }
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

      logger.logActivity(
          "API: Запрос на получение списка профилей",
          additionalData = mapOf("page" to page.toString(), "pageSize" to pageSize.toString()))

      try {
        if (page < 1 || pageSize < 1 || pageSize > 100) {
          logger.logActivity(
              "API: Некорректные параметры пагинации",
              additionalData = mapOf("page" to page.toString(), "pageSize" to pageSize.toString()))
          call.respond(
              HttpStatusCode.BadRequest,
              ErrorResponse("invalid_pagination", "Invalid pagination parameters"))
          return@get
        }

        val result = profileService.listProfiles(page, pageSize)

        logger.logActivity(
            "API: Список профилей успешно получен",
            additionalData =
                mapOf(
                    "page" to page.toString(),
                    "pageSize" to pageSize.toString(),
                    "profilesCount" to result.size.toString()))

        call.respond(result)
      } catch (e: Exception) {
        logger.logError(
            "API: Исключение при получении списка профилей: page=$page, pageSize=$pageSize",
            errorMessage = e.message ?: "Неизвестная ошибка",
            stackTrace = e.stackTraceToString())
        throw e
      }
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
