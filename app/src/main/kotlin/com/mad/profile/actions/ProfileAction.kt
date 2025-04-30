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

  private fun UserProfile.toDbMap() =
      buildMap {
        put("id", id)
        put("name", name)
        put("email", email)
        put("image_id", image_id ?: "")
        put("bio", bio ?: "")
        location?.let {
          put("country", it.country)
          put("city", it.city)
        }
        put("birthdate", "%04d-%02d-%02d".format(birthdate.year, birthdate.month, birthdate.day))
        weight?.let { put("weight", it.toString()) }
        height?.let { put("height", it.toString()) }
      }

  private fun DbProfileRow.toUserProfile(fCnt: Int, gCnt: Int) =
      UserProfile(
          id = id,
          name = name,
          email = email,
          image_id = image_id.takeIf { !it.isNullOrBlank() },
          bio = bio,
          location = if (country != null && city != null) Location(country, city) else null,
          birthdate =
              birthdate?.let {
                val p = it.split('-')
                Birthdate(p[0].toInt(), p[1].toInt(), p[2].toInt())
              }
                  ?: Birthdate(0, 0, 0),
          weight = weight?.toDoubleOrNull(),
          height = height?.toDoubleOrNull(),
          follower_count = fCnt,
          following_count = gCnt)

  override suspend fun create(profile: UserProfile): UserProfile =
      withContext(Dispatchers.IO) {
        val req = DbCreateRequest("profiles", profile.toDbMap())
        val resp: DbResponse =
            http
                .post("$baseUrl/create") {
                  contentType(ContentType.Application.Json)
                  setBody(req)
                }
                .body()
        require(resp.success == true) { "DB create failed: ${resp.error}" }
        profile
      }

  override suspend fun get(id: String): UserProfile? = fetchOne(mapOf("id" to id))

  override suspend fun getByEmail(email: String): UserProfile? = fetchOne(mapOf("email" to email))

  override suspend fun list(page: Int, pageSize: Int): List<UserProfile> =
      withContext(Dispatchers.IO) {
        val rows = callRead<DbProfileRow>("profiles", null).sortedBy { it.name }
        rows.drop((page - 1) * pageSize).take(pageSize).map {
          it.toUserProfile(followerCount(it.id), followingCount(it.id))
        }
      }

  override suspend fun update(profile: UserProfile): UserProfile? =
      withContext(Dispatchers.IO) {
        val req =
            DbUpdateRequest(
                table = "profiles",
                data = profile.toDbMap(),
                condition = "id = ?",
                conditionParams = listOf(profile.id))
        val resp: DbResponse =
            http
                .put("$baseUrl/update") {
                  contentType(ContentType.Application.Json)
                  setBody(req)
                }
                .body()
        if (resp.success != true) null else get(profile.id)
      }

  override suspend fun delete(id: String): Boolean =
      withContext(Dispatchers.IO) {
        val req = DbDeleteRequest("profiles", "id = ?", listOf(id))
        val resp: DbResponse =
            http
                .delete("$baseUrl/delete") {
                  contentType(ContentType.Application.Json)
                  setBody(req)
                }
                .body()
        resp.success == true
      }

  override suspend fun followerCount(userId: String): Int =
      callRead<DbFollowerRow>("followers", mapOf("followee_id" to userId)).size

  override suspend fun followingCount(userId: String): Int =
      callRead<DbFollowerRow>("followers", mapOf("follower_id" to userId)).size

  private suspend fun fetchOne(filters: Map<String, String>): UserProfile? {
    val row = callRead<DbProfileRow>("profiles", filters).firstOrNull() ?: return null
    return row.toUserProfile(followerCount(row.id), followingCount(row.id))
  }

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
