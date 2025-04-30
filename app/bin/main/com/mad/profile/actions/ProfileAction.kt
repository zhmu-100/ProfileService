package com.mad.profile.actions

import com.mad.feed.dto.*
import com.mad.profile.dto.*
import com.mad.profile.model.*
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Класс для работы с профилями пользователей
 *
 * @see IProfileAction
 */
class ProfileAction : IProfileAction {

  private val dotenv = dotenv()
  private val dbMode = dotenv["DB_MODE"] ?: "LOCAL"
  private val dbHost = dotenv["DB_HOST"] ?: "localhost"
  private val dbPort = dotenv["DB_PORT"] ?: "8080"
  private val baseUrl =
      if (dbMode.equals("gateway", true)) {
        "http://$dbHost:$dbPort/api/db"
      } else {
        "http://$dbHost:$dbPort"
      }

  private val http = HttpClient { install(ContentNegotiation) { json() } }

  /** Преобразует объект [UserProfile] в Map для сохранения в базе данных */
  private fun UserProfile.toDbMap() = buildMap {
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

  /** Преобразует объект [DbProfileRow] в объект [UserProfile] */
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

  /**
   * Создает новый профиль пользователя в базе данных
   *
   * @param profile объект [UserProfile], который нужно создать
   * @return созданный объект [UserProfile]
   */
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

  /**
   * Получает профиль пользователя по ID
   *
   * @param id ID профиля
   * @return объект [UserProfile] или null, если профиль не найден
   */
  override suspend fun get(id: String): UserProfile? = fetchOne(mapOf("id" to id))

  /**
   * Получает профиль пользователя по email
   *
   * @param email email профиля
   * @return объект [UserProfile] или null, если профиль не найден
   */
  override suspend fun getByEmail(email: String): UserProfile? = fetchOne(mapOf("email" to email))

  /**
   * Получает список профилей пользователей
   *
   * @param page номер страницы
   * @param pageSize размер страницы
   * @return список объектов [UserProfile]
   */
  override suspend fun list(page: Int, pageSize: Int): List<UserProfile> =
      withContext(Dispatchers.IO) {
        val rows = callRead<DbProfileRow>("profiles", null).sortedBy { it.name }
        rows.drop((page - 1) * pageSize).take(pageSize).map {
          it.toUserProfile(followerCount(it.id), followingCount(it.id))
        }
      }

  /**
   * Обновляет профиль пользователя в базе данных
   *
   * @param profile объект [UserProfile], который нужно обновить
   * @return обновленный объект [UserProfile] или null, если профиль не найден
   */
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

  /**
   * Удаляет профиль пользователя из базы данных
   *
   * @param id ID профиля
   * @return true, если удаление успешно, false в противном случае
   */
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

  /**
   * Получает количество подписчиков пользователя
   *
   * @param userId ID пользователя
   * @return количество подписчиков
   */
  override suspend fun followerCount(userId: String): Int =
      callRead<DbFollowerRow>("followers", mapOf("followee_id" to userId)).size

  /**
   * Получает количество подписок пользователя
   *
   * @param userId ID пользователя
   * @return количество подписок
   */
  override suspend fun followingCount(userId: String): Int =
      callRead<DbFollowerRow>("followers", mapOf("follower_id" to userId)).size

  /**
   * Получает профиль пользователя по фильтрам
   *
   * @param filters карта фильтров для поиска профиля
   * @return объект [UserProfile] или null, если профиль не найден
   */
  private suspend fun fetchOne(filters: Map<String, String>): UserProfile? {
    val row = callRead<DbProfileRow>("profiles", filters).firstOrNull() ?: return null
    return row.toUserProfile(followerCount(row.id), followingCount(row.id))
  }

  /** Выполняет запрос к базе данных для получения списка записей */
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
