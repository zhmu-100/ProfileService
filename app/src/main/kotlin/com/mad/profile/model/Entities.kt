package com.mad.profile.model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.date
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * Сущность профиля пользователя.
 *
 * Наследуется от [UUIDEntity], используя UUID в качестве первичного ключа.
 * Содержит все поля профиля и методы для преобразования в DTO.
 */
class ProfileEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ProfileEntity>(Profiles)

    /** Имя пользователя */
    var name by Profiles.name

    /** Email пользователя (уникальный) */
    var email by Profiles.email

    /** Идентификатор аватара пользователя */
    var imageId by Profiles.imageId

    /** Биография пользователя */
    var bio by Profiles.bio

    /** Страна проживания */
    var country by Profiles.country

    /** Город проживания */
    var city by Profiles.city

    /** Дата рождения */
    var birthdate by Profiles.birthdate

    /** Вес пользователя в кг */
    var weight by Profiles.weight

    /** Рост пользователя в см */
    var height by Profiles.height

    /** Дата создания профиля */
    var createdAt by Profiles.createdAt

    /** Дата последнего обновления профиля */
    var updatedAt by Profiles.updatedAt

    /**
     * Преобразует сущность в DTO для ответа.
     *
     * @param followerCount Количество подписчиков
     * @param followingCount Количество подписок
     * @return [ProfileResponse] с данными профиля
     */
    fun toResponse(followerCount: Int, followingCount: Int): ProfileResponse {
        return ProfileResponse(
            id = id.value.toString(),
            name = name,
            email = email,
            imageId = imageId,
            bio = bio,
            location = if (country != null && city != null) {
                LocationResponse(country!!, city!!)
            } else null,
            birthdate = if (birthdate != null) {
                BirthdateResponse(
                    birthdate!!.year,
                    birthdate!!.monthValue,
                    birthdate!!.dayOfMonth
                )
            } else null,
            weight = weight,
            height = height,
            followerCount = followerCount,
            followingCount = followingCount
        )
    }
}

// Data Transfer Objects

/**
 * DTO для ответа с данными профиля.
 *
 * @property id UUID профиля в виде строки
 * @property name Имя пользователя
 * @property email Email пользователя
 * @property imageId Идентификатор аватара (опционально)
 * @property bio Биография (опционально)
 * @property location Местоположение (опционально)
 * @property birthdate Дата рождения (опционально)
 * @property weight Вес в кг (опционально)
 * @property height Рост в см (опционально)
 * @property followerCount Количество подписчиков
 * @property followingCount Количество подписок
 */
@Serializable
data class ProfileResponse(
    val id: String,
    val name: String,
    val email: String,
    val imageId: String? = null,
    val bio: String? = null,
    val location: LocationResponse? = null,
    val birthdate: BirthdateResponse? = null,
    val weight: Double? = null,
    val height: Double? = null,
    val followerCount: Int = 0,
    val followingCount: Int = 0
)

/**
 * DTO для местоположения пользователя.
 *
 * @property country Страна
 * @property city Город
 */
@Serializable
data class LocationResponse(
    val country: String,
    val city: String
)

/**
 * DTO для даты рождения пользователя.
 *
 * @property year Год рождения
 * @property month Месяц рождения (1-12)
 * @property day День рождения
 */
@Serializable
data class BirthdateResponse(
    val year: Int,
    val month: Int,
    val day: Int
)

/**
 * DTO для запроса создания/обновления профиля.
 *
 * @property name Имя пользователя (обязательное)
 * @property email Email пользователя (обязательное)
 * @property imageId Идентификатор аватара (опционально)
 * @property bio Биография (опционально)
 * @property location Местоположение (опционально)
 * @property birthdate Дата рождения (опционально)
 * @property weight Вес в кг (опционально)
 * @property height Рост в см (опционально)
 */
@Serializable
data class ProfileRequest(
    val name: String,
    val email: String,
    val imageId: String? = null,
    val bio: String? = null,
    val location: LocationResponse? = null,
    val birthdate: BirthdateResponse? = null,
    val weight: Double? = null,
    val height: Double? = null
)

/**
 * DTO для запроса подписки/отписки.
 *
 * @property followerId UUID подписчика в виде строки
 * @property followeeId UUID пользователя для подписки в виде строки
 */
@Serializable
data class FollowRequest(
    val followerId: String,
    val followeeId: String
)

/**
 * DTO для ответа со списком подписчиков.
 *
 * @property followerIds Список UUID подписчиков
 * @property page Текущая страница
 * @property pageSize Размер страницы
 * @property total Общее количество подписчиков
 */
@Serializable
data class FollowersResponse(
    val followerIds: List<String>,
    val page: Int,
    val pageSize: Int,
    val total: Int
)

/**
 * DTO для ответа со списком подписок.
 *
 * @property followingIds Список UUID подписок
 * @property page Текущая страница
 * @property pageSize Размер страницы
 * @property total Общее количество подписок
 */
@Serializable
data class FollowingResponse(
    val followingIds: List<String>,
    val page: Int,
    val pageSize: Int,
    val total: Int
)

/**
 * DTO для ответа со списком профилей.
 *
 * @property profiles Список профилей
 * @property page Текущая страница
 * @property pageSize Размер страницы
 * @property total Общее количество профилей
 */
@Serializable
data class ProfilesResponse(
    val profiles: List<ProfileResponse>,
    val page: Int,
    val pageSize: Int,
    val total: Int
)

/**
 * DTO для ответа с ошибкой.
 *
 * @property error Тип ошибки
 * @property message Сообщение об ошибке
 */
@Serializable
data class ErrorResponse(
    val error: String,
    val message: String
)