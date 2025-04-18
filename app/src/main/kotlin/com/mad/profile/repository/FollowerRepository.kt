package com.mad.profile.repository

import java.util.UUID

/**
 * Интерфейс репозитория для управления подписками пользователей.
 *
 * Предоставляет методы для работы с отношениями "подписчик-подписка" между пользователями.
 */
interface FollowerRepository {
    /**
     * Подписаться на пользователя.
     *
     * @param followerId UUID пользователя, который инициирует подписку
     * @param followeeId UUID пользователя, на которого подписываются
     * @return `true` если подписка успешно создана,
     *         `false` если подписка уже существует или пользователь пытается подписаться на себя
     */
    suspend fun follow(followerId: UUID, followeeId: UUID): Boolean

    /**
     * Отписаться от пользователя.
     *
     * @param followerId UUID пользователя, который инициирует отписку
     * @param followeeId UUID пользователя, от которого отписываются
     * @return `true` если отписка прошла успешно,
     *         `false` если подписка не существовала
     */
    suspend fun unfollow(followerId: UUID, followeeId: UUID): Boolean

    /**
     * Получить список подписчиков пользователя с пагинацией.
     *
     * @param userId UUID пользователя, чьих подписчиков запрашивают
     * @param page номер страницы (начинается с 1)
     * @param pageSize количество элементов на странице
     * @return [Triple] содержащий:
     *         - список UUID подписчиков
     *         - текущую страницу
     *         - общее количество подписчиков
     */
    suspend fun listFollowers(userId: UUID, page: Int, pageSize: Int): Triple<List<UUID>, Int, Int>

    /**
     * Получить список подписок пользователя с пагинацией.
     *
     * @param userId UUID пользователя, чьи подписки запрашивают
     * @param page номер страницы (начинается с 1)
     * @param pageSize количество элементов на странице
     * @return [Triple] содержащий:
     *         - список UUID пользователей, на которых подписан текущий пользователь
     *         - текущую страницу
     *         - общее количество подписок
     */
    suspend fun listFollowing(userId: UUID, page: Int, pageSize: Int): Triple<List<UUID>, Int, Int>

    /**
     * Проверить наличие подписки между пользователями.
     *
     * @param followerId UUID предполагаемого подписчика
     * @param followeeId UUID пользователя, на которого проверяют подписку
     * @return `true` если подписка существует, `false` в противном случае
     */
    suspend fun isFollowing(followerId: UUID, followeeId: UUID): Boolean
}