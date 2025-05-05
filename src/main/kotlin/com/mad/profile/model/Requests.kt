package com.mad.profile.model

import kotlinx.serialization.Serializable

/**
 * Запрос на создание профиля
 *
 * @property profile Профиль пользователя
 */
@Serializable data class CreateProfileRequest(val profile: UserProfile)

/**
 * Запрос на обновление профиля
 *
 * @property profile Профиль пользователя
 */
@Serializable data class UpdateProfileRequest(val profile: UserProfile)

/**
 * Запрос на подписку
 *
 * @property follower_id ID подписчика
 * @property followee_id ID подписанного пользователя
 */
@Serializable data class FollowRequest(val follower_id: String, val followee_id: String)

/**
 * Запрос на отписку
 *
 * @property follower_id ID подписчика
 * @property followee_id ID подписанного пользователя
 */
@Serializable data class UnfollowRequest(val follower_id: String, val followee_id: String)

/**
 * Ответ списка подписчкиков
 *
 * @property follower_ids Список ID подписчиков
 */
@Serializable data class ListFollowersResponse(val follower_ids: List<String>)

/**
 * Ответ списка подписок
 *
 * @property following_ids Список ID подписок
 */
@Serializable data class ListFollowingResponse(val following_ids: List<String>)

/** Ответ - ошибка */
@Serializable data class ErrorResponse(val error: String, val message: String)
