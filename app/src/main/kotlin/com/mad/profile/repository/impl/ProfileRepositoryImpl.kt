package com.mad.profile.repository.impl

import com.mad.profile.config.dbQuery
import com.mad.profile.model.Followers
import com.mad.profile.model.ProfileEntity
import com.mad.profile.model.ProfileRequest
import com.mad.profile.model.Profiles
import com.mad.profile.repository.ProfileRepository
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class ProfileRepositoryImpl : ProfileRepository {
   
    override suspend fun getByEmail(email: String): ProfileEntity? = dbQuery {
        ProfileEntity.find { Profiles.email eq email }.firstOrNull()
    }

    override suspend fun create(profile: ProfileRequest): ProfileEntity = dbQuery {
        ProfileEntity.new {
            name = profile.name
            email = profile.email
            imageId = profile.imageId
            bio = profile.bio
            
            if (profile.location != null) {
                country = profile.location.country
                city = profile.location.city
            }
            
            if (profile.birthdate != null) {
                birthdate = LocalDate.of(
                    profile.birthdate.year,
                    profile.birthdate.month,
                    profile.birthdate.day
                )
            }
            
            weight = profile.weight
            height = profile.height
            createdAt = Instant.now()
            updatedAt = Instant.now()
        }
    }

    override suspend fun getById(id: UUID): ProfileEntity? = dbQuery {
        ProfileEntity.findById(id)
    }

    override suspend fun list(page: Int, pageSize: Int): Pair<List<ProfileEntity>, Int> = dbQuery {
        val offset = (page - 1) * pageSize
        val total = ProfileEntity.count().toInt()
        
        val profiles = ProfileEntity.all()
            .orderBy(Profiles.name to SortOrder.ASC)
            .limit(pageSize, offset.toLong())
            .toList()
            
        Pair(profiles, total)
    }

    override suspend fun update(id: UUID, profile: ProfileRequest): ProfileEntity? = dbQuery {
        val existingProfile = ProfileEntity.findById(id) ?: return@dbQuery null
        
        existingProfile.apply {
            name = profile.name
            email = profile.email
            imageId = profile.imageId
            bio = profile.bio
            
            if (profile.location != null) {
                country = profile.location.country
                city = profile.location.city
            } else {
                country = null
                city = null
            }
            
            if (profile.birthdate != null) {
                birthdate = LocalDate.of(
                    profile.birthdate.year,
                    profile.birthdate.month,
                    profile.birthdate.day
                )
            } else {
                birthdate = null
            }
            
            weight = profile.weight
            height = profile.height
            updatedAt = Instant.now()
        }
        
        existingProfile
    }

    override suspend fun delete(id: UUID): Boolean = dbQuery {
        val profile = ProfileEntity.findById(id) ?: return@dbQuery false
        
        // Delete all follower relationships
        Followers.deleteWhere { (Followers.followerId eq id) or (Followers.followeeId eq id) }
        
        // Delete the profile
        profile.delete()
        true
    }

    override suspend fun getFollowerCount(id: UUID): Int = dbQuery {
        Followers.select { Followers.followeeId eq id }.count().toInt()
    }

    override suspend fun getFollowingCount(id: UUID): Int = dbQuery {
        Followers.select { Followers.followerId eq id }.count().toInt()
    }
}