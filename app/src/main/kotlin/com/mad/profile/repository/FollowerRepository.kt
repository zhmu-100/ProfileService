package com.mad.profile.repository

import java.util.UUID

interface FollowerRepository {
    suspend fun follow(followerId: UUID, followeeId: UUID): Boolean
    suspend fun unfollow(followerId: UUID, followeeId: UUID): Boolean
    suspend fun listFollowers(userId: UUID, page: Int, pageSize: Int): Triple<List<UUID>, Int, Int>
    suspend fun listFollowing(userId: UUID, page: Int, pageSize: Int): Triple<List<UUID>, Int, Int>
    suspend fun isFollowing(followerId: UUID, followeeId: UUID): Boolean
}