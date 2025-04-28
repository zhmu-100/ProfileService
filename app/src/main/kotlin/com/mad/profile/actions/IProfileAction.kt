package com.mad.profile.actions

import com.mad.profile.model.ProfileRequest
import com.mad.profile.model.ProfileResponse
import java.util.*

interface IProfileAction {
  suspend fun createProfile(req: ProfileRequest): ProfileResponse
  suspend fun getById(id: UUID): ProfileResponse?
  suspend fun getByEmail(email: String): ProfileResponse?
  suspend fun list(page: Int, pageSize: Int): Pair<List<ProfileResponse>, Int>
  suspend fun update(id: UUID, req: ProfileRequest): ProfileResponse?
  suspend fun delete(id: UUID): Boolean
  suspend fun followerCount(id: UUID): Int
  suspend fun followingCount(id: UUID): Int
}
