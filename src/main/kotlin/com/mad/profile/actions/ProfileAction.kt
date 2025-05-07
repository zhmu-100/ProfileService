package com.mad.profile.actions

import com.mad.feed.dto.*
import com.mad.profile.dto.*
import com.mad.profile.logging.LoggerProvider
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
  private val logger = LoggerProvider.logger
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
    put("user_id", user_id)
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
          user_id = user_id,
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
        logger.logActivity(
            "Создание профиля пользователя",
            additionalData =
                mapOf(
                    "id" to profile.id,
                    "user_id" to profile.user_id,
                    "email" to profile.email,
                    "name" to profile.name))

        try {
          val req = DbCreateRequest("profiles", profile.toDbMap())
          val resp: DbResponse =
              http
                  .post("$baseUrl/create") {
                    contentType(ContentType.Application.Json)
                    setBody(req)
                  }
                  .body()

          if (resp.success != true) {
            logger.logError(
                "Ошибка при создании профиля: id=${profile.id}, user_id=${profile.user_id}, email=${profile.email}",
                errorMessage = resp.error ?: "Неизвестная ошибка")
            error("DB create failed: ${resp.error}")
          }

          logger.logActivity(
              "Профиль пользователя успешно создан",
              additionalData =
                  mapOf("id" to profile.id, "user_id" to profile.user_id, "email" to profile.email))

          profile
        } catch (e: Exception) {
          logger.logError(
              "Исключение при создании профиля: id=${profile.id}, user_id=${profile.user_id}, email=${profile.email}",
              errorMessage = e.message ?: "Неизвестная ошибка",
              stackTrace = e.stackTraceToString())
          throw e
        }
      }

  /**
   * Получает профиль пользователя по ID
   *
   * @param id ID профиля
   * @return объект [UserProfile] или null, если профиль не найден
   */
  override suspend fun get(id: String): UserProfile? {
    logger.logActivity("Получение профиля по ID", additionalData = mapOf("id" to id))

    try {
      val profile = fetchOne(mapOf("id" to id))

      if (profile == null) {
        logger.logActivity("Профиль не найден", additionalData = mapOf("id" to id))
      } else {
        logger.logActivity(
            "Профиль успешно получен",
            additionalData = mapOf("id" to id, "email" to profile.email, "name" to profile.name))
      }

      return profile
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при получении профиля: id=$id",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }

  /**
   * Получает профиль пользователя по user_id
   *
   * @param userId ID пользователя
   * @return объект [UserProfile] или null, если профиль не найден
   */
  override suspend fun getByUserId(userId: String): UserProfile? {
    logger.logActivity("Получение профиля по user_id", additionalData = mapOf("user_id" to userId))

    try {
      val profile = fetchOne(mapOf("user_id" to userId))

      if (profile == null) {
        logger.logActivity(
            "Профиль не найден по user_id", additionalData = mapOf("user_id" to userId))
      } else {
        logger.logActivity(
            "Профиль успешно получен по user_id",
            additionalData =
                mapOf(
                    "id" to profile.id,
                    "user_id" to userId,
                    "email" to profile.email,
                    "name" to profile.name))
      }

      return profile
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при получении профиля по user_id: user_id=$userId",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }

  /**
   * Получает профиль пользователя по email
   *
   * @param email email профиля
   * @return объект [UserProfile] или null, если профиль не найден
   */
  override suspend fun getByEmail(email: String): UserProfile? {
    logger.logActivity("Получение профиля по email", additionalData = mapOf("email" to email))

    try {
      val profile = fetchOne(mapOf("email" to email))

      if (profile == null) {
        logger.logActivity("Профиль не найден по email", additionalData = mapOf("email" to email))
      } else {
        logger.logActivity(
            "Профиль успешно получен по email",
            additionalData = mapOf("id" to profile.id, "email" to email, "name" to profile.name))
      }

      return profile
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при получении профиля по email: email=$email",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }

  /**
   * Получает список профилей пользователей
   *
   * @param page номер страницы
   * @param pageSize размер страницы
   * @return список объектов [UserProfile]
   */
  override suspend fun list(page: Int, pageSize: Int): List<UserProfile> =
      withContext(Dispatchers.IO) {
        logger.logActivity(
            "Получение списка профилей",
            additionalData = mapOf("page" to page.toString(), "pageSize" to pageSize.toString()))

        try {
          val rows = callRead<DbProfileRow>("profiles", null).sortedBy { it.name }
          val profiles =
              rows.drop((page - 1) * pageSize).take(pageSize).map {
                it.toUserProfile(followerCount(it.id), followingCount(it.id))
              }

          logger.logActivity(
              "Список профилей успешно получен",
              additionalData =
                  mapOf(
                      "totalProfiles" to rows.size.toString(),
                      "returnedProfiles" to profiles.size.toString()))

          profiles
        } catch (e: Exception) {
          logger.logError(
              "Ошибка при получении списка профилей: page=$page, pageSize=$pageSize",
              errorMessage = e.message ?: "Неизвестная ошибка",
              stackTrace = e.stackTraceToString())
          throw e
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
        logger.logActivity(
            "Обновление профиля пользователя",
            additionalData =
                mapOf(
                    "id" to profile.id,
                    "user_id" to profile.user_id,
                    "email" to profile.email,
                    "name" to profile.name))

        try {
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

          if (resp.success != true) {
            logger.logActivity(
                "Профиль не найден при обновлении",
                additionalData = mapOf("id" to profile.id, "user_id" to profile.user_id))
            return@withContext null
          }

          val updatedProfile = get(profile.id)

          logger.logActivity(
              "Профиль пользователя успешно обновлен",
              additionalData =
                  mapOf("id" to profile.id, "user_id" to profile.user_id, "email" to profile.email))

          updatedProfile
        } catch (e: Exception) {
          logger.logError(
              "Ошибка при обновлении профиля: id=${profile.id}, user_id=${profile.user_id}, email=${profile.email}",
              errorMessage = e.message ?: "Неизвестная ошибка",
              stackTrace = e.stackTraceToString())
          throw e
        }
      }

  /**
   * Удаляет профиль пользователя из базы данных
   *
   * @param id ID профиля
   * @return true, если удаление успешно, false в противном случае
   */
  override suspend fun delete(id: String): Boolean =
      withContext(Dispatchers.IO) {
        logger.logActivity("Удаление профиля пользователя", additionalData = mapOf("id" to id))

        try {
          val req = DbDeleteRequest("profiles", "id = ?", listOf(id))
          val resp: DbResponse =
              http
                  .delete("$baseUrl/delete") {
                    contentType(ContentType.Application.Json)
                    setBody(req)
                  }
                  .body()

          val success = resp.success == true

          if (success) {
            logger.logActivity(
                "Профиль пользователя успешно удален", additionalData = mapOf("id" to id))
          } else {
            logger.logActivity("Профиль не найден при удалении", additionalData = mapOf("id" to id))
          }

          success
        } catch (e: Exception) {
          logger.logError(
              "Ошибка при удалении профиля: id=$id",
              errorMessage = e.message ?: "Неизвестная ошибка",
              stackTrace = e.stackTraceToString())
          throw e
        }
      }

  /**
   * Получает количество подписчиков пользователя
   *
   * @param userId ID пользователя
   * @return количество подписчиков
   */
  override suspend fun followerCount(userId: String): Int {
    logger.logActivity(
        "Получение количества подписчиков", additionalData = mapOf("userId" to userId))

    try {
      val count = callRead<DbFollowerRow>("followers", mapOf("followee_id" to userId)).size

      logger.logActivity(
          "Количество подписчиков получено",
          additionalData = mapOf("userId" to userId, "followerCount" to count.toString()))

      return count
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при получении количества подписчиков: userId=$userId",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }

  /**
   * Получает количество подписок пользователя
   *
   * @param userId ID пользователя
   * @return количество подписок
   */
  override suspend fun followingCount(userId: String): Int {
    logger.logActivity("Получение количества подписок", additionalData = mapOf("userId" to userId))

    try {
      val count = callRead<DbFollowerRow>("followers", mapOf("follower_id" to userId)).size

      logger.logActivity(
          "Количество подписок получено",
          additionalData = mapOf("userId" to userId, "followingCount" to count.toString()))

      return count
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при получении количества подписок: userId=$userId",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }

  /**
   * Получает профиль пользователя по фильтрам
   *
   * @param filters карта фильтров для поиска профиля
   * @return объект [UserProfile] или null, если профиль не найден
   */
  private suspend fun fetchOne(filters: Map<String, String>): UserProfile? {
    logger.logActivity(
        "Поиск профиля по фильтрам", additionalData = mapOf("filters" to filters.toString()))

    try {
      val row = callRead<DbProfileRow>("profiles", filters).firstOrNull()

      if (row == null) {
        logger.logActivity(
            "Профиль не найден по фильтрам",
            additionalData = mapOf("filters" to filters.toString()))
        return null
      }

      val followerCnt = followerCount(row.id)
      val followingCnt = followingCount(row.id)
      val profile = row.toUserProfile(followerCnt, followingCnt)

      logger.logActivity(
          "Профиль найден по фильтрам",
          additionalData =
              mapOf("id" to profile.id, "email" to profile.email, "name" to profile.name))

      return profile
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при поиске профиля по фильтрам: filters=$filters",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }

  /** Выполняет запрос к базе данных для получения списка записей */
  private suspend inline fun <reified R> callRead(
      table: String,
      filters: Map<String, String>? = null
  ): List<R> {
    logger.logActivity(
        "Запрос к БД: чтение данных",
        additionalData = mapOf("table" to table, "filters" to (filters?.toString() ?: "null")))

    try {
      val result =
          http
              .post("$baseUrl/read") {
                contentType(ContentType.Application.Json)
                setBody(DbReadRequest(table = table, filters = filters))
              }
              .body<List<R>>()

      logger.logActivity(
          "Данные из БД получены успешно",
          additionalData = mapOf("table" to table, "rowsCount" to result.size.toString()))

      return result
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при чтении данных из БД: table=$table, filters=${filters?.toString() ?: "null"}",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }
}
