package com.mad.profile.actions

import com.mad.feed.dto.DbCreateRequest
import com.mad.feed.dto.DbDeleteRequest
import com.mad.feed.dto.DbReadRequest
import com.mad.feed.dto.DbResponse
import com.mad.profile.dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import java.time.Instant
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FollowerAction(config: ApplicationConfig) : IFollowerAction {

  private val dbMode = config.propertyOrNull("ktor.database.mode")?.getString() ?: "LOCAL"
  private val dbHost = config.propertyOrNull("ktor.database.host")?.getString() ?: "localhost"
  private val dbPort = config.propertyOrNull("ktor.database.port")?.getString() ?: "8080"
  private val baseUrl =
      if (dbMode.equals("gateway", true)) "http://$dbHost:$dbPort/api/db"
      else "http://$dbHost:$dbPort"
  private val http = HttpClient { install(ContentNegotiation) { json() } }

  override suspend fun follow(followerId: String, followeeId: String): Boolean =
      withContext(Dispatchers.IO) {
        if (followerId == followeeId) return@withContext false
        if (isFollowing(followerId, followeeId)) return@withContext false

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
        resp.success == true
      }

  override suspend fun unfollow(followerId: String, followeeId: String): Boolean =
      withContext(Dispatchers.IO) {
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
        resp.success == true
      }

  override suspend fun listFollowers(userId: String): List<String> =
      readIds(mapOf("followee_id" to userId)) { it.follower_id }

  override suspend fun listFollowing(userId: String): List<String> =
      readIds(mapOf("follower_id" to userId)) { it.followee_id }

  override suspend fun isFollowing(followerId: String, followeeId: String): Boolean =
      readIds(mapOf("follower_id" to followerId, "followee_id" to followeeId)) { it.follower_id }
          .isNotEmpty()

  private suspend fun readIds(
      filters: Map<String, String>,
      selector: (DbFollowerRow) -> String
  ): List<String> =
      http
          .post("$baseUrl/read") {
            contentType(ContentType.Application.Json)
            setBody(DbReadRequest(table = "followers", filters = filters))
          }
          .body<List<DbFollowerRow>>()
          .map(selector)
}
