package com.mad.profile.repository

import com.mad.profile.model.ProfileEntity
import com.mad.profile.model.ProfileRequest
import java.util.UUID

interface ProfileRepository {
    suspend fun create(profile: ProfileRequest): ProfileEntity
    suspend fun getById(id: UUID): ProfileEntity?   
    suspend fun list(page: Int, pageSize: Int): Pair<List<ProfileEntity>, Int>
    suspend fun update(id: UUID, profile: ProfileRequest): ProfileEntity?
    suspend fun delete(id: UUID): Boolean
    suspend fun getFollowerCount(id: UUID): Int
    suspend fun getByEmail(email: String): ProfileEntity?
    suspend fun getFollowingCount(id: UUID): Int

}