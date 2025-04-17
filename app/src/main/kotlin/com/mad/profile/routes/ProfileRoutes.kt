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

// Это вызывается из routing { ... }
fun Routing.configureProfileRoutes() {
    val profileService by inject<ProfileService>()

    route("/api/profiles") {

        // Создание профиля
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

        // Получение профиля по ID
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

        // Обновление профиля
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

        // Удаление профиля
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

        // Получение списка профилей с пагинацией
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

// Email validation
private fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    return email.matches(emailRegex.toRegex())
}
