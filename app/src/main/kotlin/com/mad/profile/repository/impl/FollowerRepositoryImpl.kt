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

/**
 * Реализация репозитория для работы с подписками пользователей.
 *
 * Позволяет управлять отношениями "подписчик-подписка" между пользователями.
 */
class FollowerRepositoryImpl : FollowerRepository {

    /**
     * Подписаться на пользователя.
     *
     * @param followerId UUID пользователя, который хочет подписаться
     * @param followeeId UUID пользователя, на которого подписываются
     * @return `true` если подписка успешно создана, `false` если:
     *         - пользователь пытается подписаться на себя
     *         - подписка уже существует
     */
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

    /**
     * Отписаться от пользователя.
     *
     * @param followerId UUID пользователя, который хочет отписаться
     * @param followeeId UUID пользователя, от которого отписываются
     * @return `true` если отписка прошла успешно, `false` если подписка не существовала
     */
    override suspend fun unfollow(followerId: UUID, followeeId: UUID): Boolean = dbQuery {
        val deleted = Followers.deleteWhere {
            (Followers.followerId eq followerId) and (Followers.followeeId eq followeeId)
        }
        
        deleted > 0
    }

    /**
     * Получить список подписчиков пользователя с пагинацией.
     *
     * @param userId UUID пользователя, чьих подписчиков запрашивают
     * @param page номер страницы (начинается с 1)
     * @param pageSize количество элементов на странице
     * @return Triple содержащий:
     *         - список UUID подписчиков
     *         - текущую страницу
     *         - общее количество подписчиков
     */
    override suspend fun listFollowers(userId: UUID, page: Int, pageSize: Int): Triple<List<UUID>, Int, Int> = dbQuery {
        val offset = (page - 1) * pageSize
        
        val total = Followers.select { Followers.followeeId eq userId }.count().toInt()
        
        val followers = Followers.select { Followers.followeeId eq userId }
            .limit(pageSize, offset.toLong())
            .map { it[Followers.followerId].value }
            
        Triple(followers, page, total)
    }

    /**
     * Получить список подписок пользователя с пагинацией.
     *
     * @param userId UUID пользователя, чьи подписки запрашивают
     * @param page номер страницы (начинается с 1)
     * @param pageSize количество элементов на странице
     * @return Triple содержащий:
     *         - список UUID пользователей, на которых подписан текущий пользователь
     *         - текущую страницу
     *         - общее количество подписок
     */
    override suspend fun listFollowing(userId: UUID, page: Int, pageSize: Int): Triple<List<UUID>, Int, Int> = dbQuery {
        val offset = (page - 1) * pageSize
        
        val total = Followers.select { Followers.followerId eq userId }.count().toInt()
        
        val following = Followers.select { Followers.followerId eq userId }
            .limit(pageSize, offset.toLong())
            .map { it[Followers.followeeId].value }
            
        Triple(following, page, total)
    }

    /**
     * Проверить, подписан ли один пользователь на другого.
     *
     * @param followerId UUID предполагаемого подписчика
     * @param followeeId UUID пользователя, на которого проверяют подписку
     * @return `true` если подписка существует, `false` в противном случае
     */
    override suspend fun isFollowing(followerId: UUID, followeeId: UUID): Boolean = dbQuery {
        Followers.select {
            (Followers.followerId eq followerId) and (Followers.followeeId eq followeeId)
        }.count() > 0
    }
}