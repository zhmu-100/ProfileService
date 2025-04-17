package com.mad.profile.service

import com.mad.profile.model.*
import java.util.UUID

interface ProfileService {
    suspend fun createProfile(profile: ProfileRequest): ProfileResponse
    suspend fun getProfile(id: UUID): ProfileResponse?
    suspend fun listProfiles(page: Int, pageSize: Int): ProfilesResponse
    suspend fun updateProfile(id: UUID, profile: ProfileRequest): ProfileResponse?
    suspend fun deleteProfile(id: UUID): Boolean
    suspend fun follow(followerId: UUID, followeeId: UUID): Boolean
    suspend fun unfollow(followerId: UUID, followeeId: UUID): Boolean
    suspend fun listFollowers(userId: UUID, page: Int, pageSize: Int): FollowersResponse
    suspend fun listFollowing(userId: UUID, page: Int, pageSize: Int): FollowingResponse
}