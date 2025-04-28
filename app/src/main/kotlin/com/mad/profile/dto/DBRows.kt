package com.mad.profile.dto

import kotlinx.serialization.Serializable

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
    val height: String? = null,
    val created_at: String,
    val updated_at: String
)

@Serializable
data class DbFollowerRow(val follower_id: String, val followee_id: String, val created_at: String)
