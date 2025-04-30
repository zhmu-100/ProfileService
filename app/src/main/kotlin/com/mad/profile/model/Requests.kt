package com.mad.profile.model

import kotlinx.serialization.Serializable

@Serializable data class CreateProfileRequest(val profile: UserProfile)

@Serializable data class GetProfileRequest(val id: String)

@Serializable data class UpdateProfileRequest(val profile: UserProfile)

@Serializable data class DeleteProfileRequest(val id: String)

@Serializable data class FollowRequest(val follower_id: String, val followee_id: String)

@Serializable data class UnfollowRequest(val follower_id: String, val followee_id: String)

@Serializable data class ListFollowersResponse(val follower_ids: List<String>)

@Serializable data class ListFollowingResponse(val following_ids: List<String>)

@Serializable data class ErrorResponse(val error: String, val message: String)
