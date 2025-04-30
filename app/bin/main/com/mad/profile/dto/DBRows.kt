package com.mad.profile.dto

import kotlinx.serialization.Serializable

/**
 * Строка профиля в БД
 *
 * @property id ID профиля
 * @property name Имя пользователя
 * @property email Email пользователя
 * @property image_id ID изображения профиля
 * @property bio Биография пользователя
 * @property country Страна пользователя
 * @property city Город пользователя
 * @property birthdate Дата рождения пользователя
 * @property weight Вес пользователя
 * @property height Рост пользователя
 */
@Serializable
data class DbProfileRow(
    val id: String,
    val name: String,
    val email: String,
    val image_id: String? = null,
    val bio: String? = null,
    val country: String? = null,
    val city: String? = null,
    val birthdate: String? = null,
    val weight: String? = null,
    val height: String? = null
)

/**
 * Строка подписки в БД
 *
 * @property follower_id ID подписчика
 * @property followee_id ID подписанного пользователя
 * @property created_at Дата создания подписки
 */
@Serializable
data class DbFollowerRow(val follower_id: String, val followee_id: String, val created_at: String)
