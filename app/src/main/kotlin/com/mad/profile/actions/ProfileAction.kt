package com.mad.profile.actions

import com.mad.feed.dto.*
import com.mad.profile.dto.*
import com.mad.profile.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileAction(config: ApplicationConfig) : IProfileAction {

  private val dbMode = config.propertyOrNull("ktor.database.mode")?.getString() ?: "LOCAL"
  private val dbHost = config.propertyOrNull("ktor.database.host")?.getString() ?: "localhost"
  private val dbPort = config.propertyOrNull("ktor.database.port")?.getString() ?: "8080"
  private val baseUrl =
      if (dbMode.equals("gateway", true)) "http://$dbHost:$dbPort/api/db"
      else "http://$dbHost:$dbPort"

  private val http = HttpClient { install(ContentNegotiation) { json() } }

  private fun DbProfileRow.toResponse(followers: Int, following: Int) =
      ProfileResponse(
          id = id,
          name = name,
          email = email,
          imageId = image_id.takeIf { !it.isNullOrBlank() },
          bio = bio,
          location = if (country != null && city != null) LocationResponse(country, city) else null,
          birthdate =
              birthdate?.let {
                val parts = it.split('-')
                BirthdateResponse(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
              },
          weight = weight?.toDoubleOrNull(),
          height = height?.toDoubleOrNull(),
          followerCount = followers,
          followingCount = following)

  override suspend fun createProfile(req: ProfileRequest): ProfileResponse =
      withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val create = DbCreateRequest("profiles", req.toDbMap(id))

        val resp: DbResponse =
            http
                .post("$baseUrl/create") {
                  contentType(ContentType.Application.Json)
                  setBody(create)
                }
                .body()

        require(resp.success == true) { "DB create failed: ${resp.error}" }
        req.toResponseSkeleton(id)
      }

  private fun ProfileRequest.toResponseSkeleton(id: String) =
      ProfileResponse(
          id = id,
          name = name,
          email = email,
          imageId = imageId,
          bio = bio,
          location = location,
          birthdate = birthdate,
          weight = weight,
          height = height,
          followerCount = 0,
          followingCount = 0)

  override suspend fun getById(id: UUID): ProfileResponse? = fetchOne(mapOf("id" to id.toString()))
  override suspend fun getByEmail(email: String): ProfileResponse? =
      fetchOne(mapOf("email" to email))

  private suspend fun fetchOne(filters: Map<String, String>): ProfileResponse? {
    val row = callRead<DbProfileRow>("profiles", filters).firstOrNull() ?: return null
    val uuid = UUID.fromString(row.id)
    return row.toResponse(followerCount(uuid), followingCount(uuid))
  }

  override suspend fun list(page: Int, pageSize: Int): Pair<List<ProfileResponse>, Int> =
      withContext(Dispatchers.IO) {
        val rows = callRead<DbProfileRow>("profiles", null).sortedBy { it.name }

        val total = rows.size
        val slice = rows.drop((page - 1) * pageSize).take(pageSize)
        val profiles =
            slice.map {
              val uuid = UUID.fromString(it.id)
              it.toResponse(followerCount(uuid), followingCount(uuid))
            }
        Pair(profiles, total)
      }

  override suspend fun update(id: UUID, req: ProfileRequest): ProfileResponse? =
      withContext(Dispatchers.IO) {
        val update =
            DbUpdateRequest(
                table = "profiles",
                data = req.toDbMap(id.toString()),
                condition = "id = ?",
                conditionParams = listOf(id.toString()))

        val resp: DbResponse =
            http
                .put("$baseUrl/update") {
                  contentType(ContentType.Application.Json)
                  setBody(update)
                }
                .body()

        if (resp.success != true) return@withContext null
        fetchOne(mapOf("id" to id.toString()))
      }

  override suspend fun delete(id: UUID): Boolean =
      withContext(Dispatchers.IO) {
        val del = DbDeleteRequest("profiles", "id = ?", listOf(id.toString()))
        val resp: DbResponse =
            http
                .delete("$baseUrl/delete") {
                  contentType(ContentType.Application.Json)
                  setBody(del)
                }
                .body()
        resp.success == true
      }

  override suspend fun followerCount(id: UUID): Int =
      callRead<DbFollowerRow>("followers", mapOf("followee_id" to id.toString())).size

  override suspend fun followingCount(id: UUID): Int =
      callRead<DbFollowerRow>("followers", mapOf("follower_id" to id.toString())).size

  private suspend inline fun <reified R> callRead(
      table: String,
      filters: Map<String, String>? = null
  ): List<R> =
      http
          .post("$baseUrl/read") {
            contentType(ContentType.Application.Json)
            setBody(DbReadRequest(table = table, filters = filters))
          }
          .body()
}
