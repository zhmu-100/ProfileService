package com.mad.profile.routes

import com.mad.profile.model.ErrorResponse
import com.mad.profile.model.ProfileRequest
import com.mad.profile.service.ProfileService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import org.koin.ktor.ext.inject
import java.util.*

private val logger = KotlinLogging.logger {}

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
    val profileService by inject<ProfileService>()

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
            println("✅ POST /api/profiles called")
            try {
                val profileRequest = call.receive<ProfileRequest>()

                if (profileRequest.name.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("validation_error", "Name cannot be empty")
                    )
                    return@post
                }

                if (profileRequest.email.isBlank() || !isValidEmail(profileRequest.email)) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("validation_error", "Invalid email format")
                    )
                    return@post
                }

                val profile = profileService.createProfile(profileRequest)
                call.respond(HttpStatusCode.Created, profile)
            } catch (e: Exception) {
                logger.error(e) { "Error creating profile" }
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("server_error", "Failed to create profile")
                )
            }
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
            val idParam = call.parameters["id"]
            val id = try {
                UUID.fromString(idParam)
            } catch (e: Exception) {
                null
            }

            if (id == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("invalid_id", "Invalid UUID format")
                )
                return@get
            }

            val profile = profileService.getProfile(id)
            if (profile == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("not_found", "Profile not found"))
            } else {
                call.respond(HttpStatusCode.OK, profile)
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
            val idParam = call.parameters["id"]
            val id = try {
                UUID.fromString(idParam)
            } catch (e: Exception) {
                null
            }

            if (id == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("invalid_id", "Invalid UUID format")
                )
                return@put
            }

            val profileRequest = call.receive<ProfileRequest>()

            val updated = profileService.updateProfile(id, profileRequest)
            if (updated == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("not_found", "Profile not found"))
            } else {
                call.respond(HttpStatusCode.OK, updated)
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
            val idParam = call.parameters["id"]
            val id = try {
                UUID.fromString(idParam)
            } catch (e: Exception) {
                null
            }

            if (id == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("invalid_id", "Invalid UUID format")
                )
                return@delete
            }

            val success = profileService.deleteProfile(id)
            if (success) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("not_found", "Profile not found"))
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

            if (page < 1 || pageSize < 1 || pageSize > 100) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("invalid_pagination", "Invalid pagination parameters")
                )
                return@get
            }

            val result = profileService.listProfiles(page, pageSize)
            call.respond(HttpStatusCode.OK, result)
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
