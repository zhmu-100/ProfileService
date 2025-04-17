package com.mad.profile.routes

import com.mad.profile.model.ErrorResponse
import com.mad.profile.model.FollowRequest
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

// Эта функция теперь безопасно регистрируется через Routing
fun Routing.configureFollowerRoutes() {
    val profileService by inject<ProfileService>()

    route("/api/profiles") {

        // Follow a user
        post("/{id}/follow") {
            try {
                val followeeId = call.parameters["id"]?.let {
                    runCatching { UUID.fromString(it) }.getOrNull()
                }

                if (followeeId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("invalid_id", "Invalid profile ID format")
                    )
                    return@post
                }

                val followRequest = call.receive<FollowRequest>()
                val followerId = runCatching { UUID.fromString(followRequest.followerId) }.getOrNull()

                if (followerId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("invalid_id", "Invalid follower ID format")
                    )
                    return@post
                }

                val success = profileService.follow(followerId, followeeId)
                if (success) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("follow_error", "Failed to follow user")
                    )
                }
            } catch (e: Exception) {
                logger.error(e) { "Error following user" }
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("server_error", "Failed to follow user")
                )
            }
        }

        // Unfollow a user
        post("/{id}/unfollow") {
            try {
                val followeeIdFromUrl = call.parameters["id"]?.let {
                    runCatching { UUID.fromString(it) }.getOrNull()
                }
        
                if (followeeIdFromUrl == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("invalid_id", "Invalid profile ID format")
                    )
                    return@post
                }
        
                val unfollowRequest = call.receive<FollowRequest>()
                val followerId = runCatching { UUID.fromString(unfollowRequest.followerId) }.getOrNull()
                val followeeIdFromBody = runCatching { UUID.fromString(unfollowRequest.followeeId) }.getOrNull()
        
                if (followerId == null || followeeIdFromBody == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("invalid_id", "Invalid UUID format in body")
                    )
                    return@post
                }
        
                // Вот эта проверка ключевая
                if (followeeIdFromUrl != followeeIdFromBody) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("mismatch_id", "Followee ID in path and body do not match")
                    )
                    return@post
                }
        
                val success = profileService.unfollow(followerId, followeeIdFromUrl)
        
                if (success) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("unfollow_error", "Failed to unfollow user")
                    )
                }
            } catch (e: Exception) {
                logger.error(e) { "Error unfollowing user" }
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("server_error", "Failed to unfollow user")
                )
            }
        }
        

        // List followers
        get("/{id}/followers") {
            try {
                val userId = call.parameters["id"]?.let {
                    runCatching { UUID.fromString(it) }.getOrNull()
                }

                if (userId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("invalid_id", "Invalid profile ID format")
                    )
                    return@get
                }

                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 10

                if (page < 1 || pageSize < 1 || pageSize > 100) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("invalid_pagination", "Invalid pagination parameters")
                    )
                    return@get
                }

                val followers = profileService.listFollowers(userId, page, pageSize)
                call.respond(HttpStatusCode.OK, followers)
            } catch (e: Exception) {
                logger.error(e) { "Error listing followers" }
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("server_error", "Failed to list followers")
                )
            }
        }

        // List following
        get("/{id}/following") {
            try {
                val userId = call.parameters["id"]?.let {
                    runCatching { UUID.fromString(it) }.getOrNull()
                }

                if (userId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("invalid_id", "Invalid profile ID format")
                    )
                    return@get
                }

                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 10

                if (page < 1 || pageSize < 1 || pageSize > 100) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("invalid_pagination", "Invalid pagination parameters")
                    )
                    return@get
                }

                val following = profileService.listFollowing(userId, page, pageSize)
                call.respond(HttpStatusCode.OK, following)
            } catch (e: Exception) {
                logger.error(e) { "Error listing following" }
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("server_error", "Failed to list following")
                )
            }
        }
    }
}
