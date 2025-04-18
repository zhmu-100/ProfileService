
package com.mad.profile.service.impl

import com.mad.profile.model.*
import com.mad.profile.repository.FollowerRepository
import com.mad.profile.repository.ProfileRepository
import com.mad.profile.service.ProfileService
import mu.KotlinLogging
import java.util.UUID

private val logger = KotlinLogging.logger {}

/**
 * Реализация сервиса для работы с профилями пользователей и подписками.
 *
 * @property profileRepository репозиторий для работы с профилями пользователей
 * @property followerRepository репозиторий для работы с подписками пользователей
 */
class ProfileServiceImpl(
    private val profileRepository: ProfileRepository,
    private val followerRepository: FollowerRepository
) : ProfileService {

    /**
     * Создает новый профиль пользователя.
     * @param profile Данные для создания профиля
     * @return Созданный профиль [ProfileResponse]. Если профиль с таким email уже существует,
     *         возвращает существующий профиль.
     */
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

    /**
     * Получает профиль пользователя по ID.
     * @param id UUID идентификатор пользователя
     * @return Профиль [ProfileResponse] или null, если не найден
     */
    override suspend fun getProfile(id: UUID): ProfileResponse? {
        logger.info { "Getting profile with ID: $id" }
        
        val profile = profileRepository.getById(id) ?: return null
        val followerCount = profileRepository.getFollowerCount(id)
        val followingCount = profileRepository.getFollowingCount(id)
        
        return profile.toResponse(followerCount, followingCount)
    }

    /**
     * Получает список профилей с пагинацией.
     * @param page Номер страницы (начиная с 0)
     * @param pageSize Количество элементов на странице
     * @return Ответ со списком профилей [ProfilesResponse]
     */
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

    /**
     * Обновляет данные профиля пользователя.
     * @param id UUID идентификатор пользователя
     * @param profile Новые данные профиля
     * @return Обновленный профиль [ProfileResponse] или null, если профиль не найден
     */
    override suspend fun updateProfile(id: UUID, profile: ProfileRequest): ProfileResponse? {
        logger.info { "Updating profile with ID: $id" }
        
        val updatedProfile = profileRepository.update(id, profile) ?: return null
        val followerCount = profileRepository.getFollowerCount(id)
        val followingCount = profileRepository.getFollowingCount(id)
        
        return updatedProfile.toResponse(followerCount, followingCount)
    }

    /**
     * Удаляет профиль пользователя.
     * @param id UUID идентификатор пользователя
     * @return true если удаление прошло успешно, false в противном случае
     */
    override suspend fun deleteProfile(id: UUID): Boolean {
        logger.info { "Deleting profile with ID: $id" }
        
        return profileRepository.delete(id)
    }

    /**
     * Оформляет подписку одного пользователя на другого.
     * @param followerId UUID идентификатор подписчика
     * @param followeeId UUID идентификатор того, на кого подписываются
     * @return true если подписка оформлена успешно, false если один из пользователей не существует
     */
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

    /**
     * Отменяет подписку одного пользователя на другого.
     * @param followerId UUID идентификатор подписчика
     * @param followeeId UUID идентификатор того, на кого была подписка
     * @return true если отписка прошла успешно, false в противном случае
     */
    override suspend fun unfollow(followerId: UUID, followeeId: UUID): Boolean {
        logger.info { "User $followerId is unfollowing user $followeeId" }
        
        return followerRepository.unfollow(followerId, followeeId)
    }

    /**
     * Получает список подписчиков пользователя с пагинацией.
     * @param userId UUID идентификатор пользователя
     * @param page Номер страницы (начиная с 0)
     * @param pageSize Количество элементов на странице
     * @return Ответ со списком подписчиков [FollowersResponse]
     */
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

    /**
     * Получает список подписок пользователя с пагинацией.
     * @param userId UUID идентификатор пользователя
     * @param page Номер страницы (начиная с 0)
     * @param pageSize Количество элементов на странице
     * @return Ответ со списком подписок [FollowingResponse]
     */
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