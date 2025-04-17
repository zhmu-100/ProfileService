
package com.mad.profile.service.impl

import com.mad.profile.model.*
import com.mad.profile.repository.FollowerRepository
import com.mad.profile.repository.ProfileRepository
import com.mad.profile.service.ProfileService
import mu.KotlinLogging
import java.util.UUID

private val logger = KotlinLogging.logger {}

class ProfileServiceImpl(
    private val profileRepository: ProfileRepository,
    private val followerRepository: FollowerRepository
) : ProfileService {
    
    override suspend fun createProfile(profile: ProfileRequest): ProfileResponse {
        logger.info { "Creating profile for ${profile.email}" }
    
        val existing = profileRepository.getByEmail(profile.email)
        if (existing != null) {
            logger.warn { "Email already exists: ${profile.email}" }
            return existing.toResponse(
                profileRepository.getFollowerCount(existing.id.value),
                profileRepository.getFollowingCount(existing.id.value)
            )
        }
    
        val created = profileRepository.create(profile)
        return created.toResponse(0, 0)
    }
    
    
    override suspend fun getProfile(id: UUID): ProfileResponse? {
        logger.info { "Getting profile with ID: $id" }
        
        val profile = profileRepository.getById(id) ?: return null
        val followerCount = profileRepository.getFollowerCount(id)
        val followingCount = profileRepository.getFollowingCount(id)
        
        return profile.toResponse(followerCount, followingCount)
    }

    override suspend fun listProfiles(page: Int, pageSize: Int): ProfilesResponse {
        logger.info { "Listing profiles: page=$page, pageSize=$pageSize" }
        
        val (profiles, total) = profileRepository.list(page, pageSize)
        
        val profileResponses = profiles.map { profile ->
            val followerCount = profileRepository.getFollowerCount(profile.id.value)
            val followingCount = profileRepository.getFollowingCount(profile.id.value)
            profile.toResponse(followerCount, followingCount)
        }
        
        return ProfilesResponse(
            profiles = profileResponses,
            page = page,
            pageSize = pageSize,
            total = total
        )
    }

    override suspend fun updateProfile(id: UUID, profile: ProfileRequest): ProfileResponse? {
        logger.info { "Updating profile with ID: $id" }
        
        val updatedProfile = profileRepository.update(id, profile) ?: return null
        val followerCount = profileRepository.getFollowerCount(id)
        val followingCount = profileRepository.getFollowingCount(id)
        
        return updatedProfile.toResponse(followerCount, followingCount)
    }

    override suspend fun deleteProfile(id: UUID): Boolean {
        logger.info { "Deleting profile with ID: $id" }
        
        return profileRepository.delete(id)
    }

    override suspend fun follow(followerId: UUID, followeeId: UUID): Boolean {
        logger.info { "User $followerId is following user $followeeId" }
        
        val follower = profileRepository.getById(followerId)
        val followee = profileRepository.getById(followeeId)
        
        if (follower == null || followee == null) {
            logger.warn { "Follow failed: One or both users don't exist" }
            return false
        }
        
        return followerRepository.follow(followerId, followeeId)
    }

    override suspend fun unfollow(followerId: UUID, followeeId: UUID): Boolean {
        logger.info { "User $followerId is unfollowing user $followeeId" }
        
        return followerRepository.unfollow(followerId, followeeId)
    }

    override suspend fun listFollowers(userId: UUID, page: Int, pageSize: Int): FollowersResponse {
        logger.info { "Listing followers for user $userId: page=$page, pageSize=$pageSize" }
        
        val (followerIds, currentPage, total) = followerRepository.listFollowers(userId, page, pageSize)
        
        return FollowersResponse(
            followerIds = followerIds.map { it.toString() },
            page = currentPage,
            pageSize = pageSize,
            total = total
        )
    }

    override suspend fun listFollowing(userId: UUID, page: Int, pageSize: Int): FollowingResponse {
        logger.info { "Listing following for user $userId: page=$page, pageSize=$pageSize" }
        
        val (followingIds, currentPage, total) = followerRepository.listFollowing(userId, page, pageSize)
        
        return FollowingResponse(
            followingIds = followingIds.map { it.toString() },
            page = currentPage,
            pageSize = pageSize,
            total = total
        )
    }
}