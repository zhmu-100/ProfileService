package com.mad.profile.repository.impl

import com.mad.profile.config.dbQuery
import com.mad.profile.model.Followers
import com.mad.profile.repository.FollowerRepository
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.util.UUID

class FollowerRepositoryImpl : FollowerRepository {
    override suspend fun follow(followerId: UUID, followeeId: UUID): Boolean = dbQuery {
        // Check if user is not following themselves
        if (followerId == followeeId) {
            return@dbQuery false
        }
        
        // Check if the follow relationship already exists
        val exists = Followers.select {
            (Followers.followerId eq followerId) and (Followers.followeeId eq followeeId)
        }.count() > 0
        
        if (exists) {
            return@dbQuery false
        }
        
        // Create the follow relationship
        Followers.insert {
            it[Followers.followerId] = followerId
            it[Followers.followeeId] = followeeId
        }
        
        true
    }

    override suspend fun unfollow(followerId: UUID, followeeId: UUID): Boolean = dbQuery {
        val deleted = Followers.deleteWhere {
            (Followers.followerId eq followerId) and (Followers.followeeId eq followeeId)
        }
        
        deleted > 0
    }

    override suspend fun listFollowers(userId: UUID, page: Int, pageSize: Int): Triple<List<UUID>, Int, Int> = dbQuery {
        val offset = (page - 1) * pageSize
        
        val total = Followers.select { Followers.followeeId eq userId }.count().toInt()
        
        val followers = Followers.select { Followers.followeeId eq userId }
            .limit(pageSize, offset.toLong())
            .map { it[Followers.followerId].value }
            
        Triple(followers, page, total)
    }

    override suspend fun listFollowing(userId: UUID, page: Int, pageSize: Int): Triple<List<UUID>, Int, Int> = dbQuery {
        val offset = (page - 1) * pageSize
        
        val total = Followers.select { Followers.followerId eq userId }.count().toInt()
        
        val following = Followers.select { Followers.followerId eq userId }
            .limit(pageSize, offset.toLong())
            .map { it[Followers.followeeId].value }
            
        Triple(following, page, total)
    }

    override suspend fun isFollowing(followerId: UUID, followeeId: UUID): Boolean = dbQuery {
        Followers.select {
            (Followers.followerId eq followerId) and (Followers.followeeId eq followeeId)
        }.count() > 0
    }
}