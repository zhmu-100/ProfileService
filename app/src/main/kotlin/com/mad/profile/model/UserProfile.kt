package com.mad.profile.model

import kotlinx.serialization.Serializable

@Serializable data class Location(val country: String, val city: String)

@Serializable data class Birthdate(val year: Int, val month: Int, val day: Int)

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
