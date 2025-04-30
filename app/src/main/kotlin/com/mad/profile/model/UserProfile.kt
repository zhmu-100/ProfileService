package com.mad.profile.model

import kotlinx.serialization.Serializable

/**
 * Модель локации
 *
 * @property country Страна
 * @property city Город
 */
@Serializable data class Location(val country: String, val city: String)

/**
 * Модель даты рождения
 *
 * @property year Год
 * @property month Месяц
 * @property day День
 */
@Serializable data class Birthdate(val year: Int, val month: Int, val day: Int)

/**
 * Модель профиля пользователя
 *
 * @property id ID профиля
 * @property name Имя пользователя
 * @property email Email пользователя
 * @property image_id ID изображения профиля
 * @property bio Биография пользователя
 * @property location Локация пользователя
 * @property birthdate Дата рождения пользователя
 * @property weight Вес пользователя
 * @property height Рост пользователя
 * @property follower_count Количество подписчиков
 * @property following_count Количество подписок
 */
@Serializable
data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val image_id: String? = null,
    val bio: String? = null,
    val location: Location? = null,
    val birthdate: Birthdate,
    val weight: Double? = null,
    val height: Double? = null,
    val follower_count: Int = 0,
    val following_count: Int = 0
)
