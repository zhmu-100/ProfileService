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

class ProfileEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ProfileEntity>(Profiles)
    
    var name by Profiles.name
    var email by Profiles.email
    var imageId by Profiles.imageId
    var bio by Profiles.bio
    var country by Profiles.country
    var city by Profiles.city
    var birthdate by Profiles.birthdate
    var weight by Profiles.weight
    var height by Profiles.height
    var createdAt by Profiles.createdAt
    var updatedAt by Profiles.updatedAt
    
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

// Data transfer objects
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

@Serializable
data class LocationResponse(
    val country: String,
    val city: String
)

@Serializable
data class BirthdateResponse(
    val year: Int,
    val month: Int,
    val day: Int
)

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

@Serializable
data class FollowRequest(
    val followerId: String,
    val followeeId: String
)

@Serializable
data class FollowersResponse(
    val followerIds: List<String>,
    val page: Int,
    val pageSize: Int,
    val total: Int
)

@Serializable
data class FollowingResponse(
    val followingIds: List<String>,
    val page: Int,
    val pageSize: Int,
    val total: Int
)

@Serializable
data class ProfilesResponse(
    val profiles: List<ProfileResponse>,
    val page: Int,
    val pageSize: Int,
    val total: Int
)

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String
)