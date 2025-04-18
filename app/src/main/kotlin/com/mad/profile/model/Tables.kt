package com.mad.profile.model

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Таблица профилей пользователей.
 *
 * Содержит основную информацию о пользователях системы.
 * Наследуется от [UUIDTable], где UUID является первичным ключом.
 */
object Profiles : UUIDTable("profiles") {
    /**
     * Имя пользователя.
     * Максимальная длина: 255 символов.
     */
    val name: Column<String> = varchar("name", 255)

    /**
     * Уникальный email пользователя.
     * Максимальная длина: 255 символов.
     * Имеет уникальный индекс для предотвращения дублирования.
     */
    val email: Column<String> = varchar("email", 255).uniqueIndex()

    /**
     * Идентификатор аватара пользователя.
     * Может быть null, если аватар не установлен.
     * Максимальная длина: 255 символов.
     */
    val imageId: Column<String?> = varchar("image_id", 255).nullable()

    /**
     * Биография пользователя.
     * Текст произвольной длины.
     * Может быть null, если биография не заполнена.
     */
    val bio: Column<String?> = text("bio").nullable()

    /**
     * Страна проживания пользователя.
     * Максимальная длина: 100 символов.
     * Может быть null, если страна не указана.
     */
    val country: Column<String?> = varchar("country", 100).nullable()

    /**
     * Город проживания пользователя.
     * Максимальная длина: 100 символов.
     * Может быть null, если город не указан.
     */
    val city: Column<String?> = varchar("city", 100).nullable()

    /**
     * Дата рождения пользователя.
     * Может быть null, если дата рождения не указана.
     */
    val birthdate: Column<LocalDate?> = date("birthdate").nullable()

    /**
     * Вес пользователя в килограммах.
     * Может быть null, если вес не указан.
     */
    val weight: Column<Double?> = double("weight").nullable()

    /**
     * Рост пользователя в сантиметрах.
     * Может быть null, если рост не указан.
     */
    val height: Column<Double?> = double("height").nullable()

    /**
     * Дата и время создания профиля.
     * По умолчанию устанавливается текущее время.
     */
    val createdAt: Column<Instant> = timestamp("created_at").default(Instant.now())

    /**
     * Дата и время последнего обновления профиля.
     * По умолчанию устанавливается текущее время.
     */
    val updatedAt: Column<Instant> = timestamp("updated_at").default(Instant.now())
}

/**
 * Таблица подписчиков.
 *
 * Содержит информацию о том, кто на кого подписан.
 * Реализует отношение many-to-many между пользователями.
 */
object Followers : Table("followers") {
    /**
     * Идентификатор пользователя-подписчика.
     * Внешний ключ, ссылается на таблицу [Profiles].
     */
    val followerId = reference("follower_id", Profiles)

    /**
     * Идентификатор пользователя, на которого подписались.
     * Внешний ключ, ссылается на таблицу [Profiles].
     */
    val followeeId = reference("followee_id", Profiles)

    /**
     * Дата и время создания подписки.
     * По умолчанию устанавливается текущее время.
     */
    val createdAt = timestamp("created_at").default(Instant.now())

    /**
     * Составной первичный ключ из followerId и followeeId.
     * Гарантирует уникальность пары подписчик-подписка.
     */
    override val primaryKey = PrimaryKey(followerId, followeeId)
}