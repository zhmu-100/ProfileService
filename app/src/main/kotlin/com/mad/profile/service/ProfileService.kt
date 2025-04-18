package com.mad.profile.service

import com.mad.profile.model.*
import java.util.UUID

/**
 * Сервис для работы с профилями пользователей и подписками.
 *
 * Предоставляет CRUD операции для профилей, а также функционал подписок/отписок
 * и получения списков подписчиков/подписок.
 */
interface ProfileService {
    /**
     * Создает новый профиль пользователя.
     * @param profile Данные для создания профиля
     * @return Созданный профиль в формате [ProfileResponse]
     */
    suspend fun createProfile(profile: ProfileRequest): ProfileResponse

    /**
     * Получает профиль пользователя по идентификатору.
     * @param id UUID идентификатор пользователя
     * @return Профиль в формате [ProfileResponse] или null, если не найден
     */
    suspend fun getProfile(id: UUID): ProfileResponse?

    /**
     * Получает список профилей с пагинацией.
     * @param page Номер страницы (начиная с 0)
     * @param pageSize Количество элементов на странице
     * @return Ответ со списком профилей [ProfilesResponse]
     */
    suspend fun listProfiles(page: Int, pageSize: Int): ProfilesResponse

    /**
     * Обновляет данные профиля пользователя.
     * @param id UUID идентификатор пользователя
     * @param profile Новые данные профиля
     * @return Обновленный профиль [ProfileResponse] или null, если профиль не найден
     */
    suspend fun updateProfile(id: UUID, profile: ProfileRequest): ProfileResponse?

    /**
     * Удаляет профиль пользователя.
     * @param id UUID идентификатор пользователя
     * @return true если удаление прошло успешно, false в противном случае
     */
    suspend fun deleteProfile(id: UUID): Boolean

    /**
     * Оформляет подписку одного пользователя на другого.
     * @param followerId UUID идентификатор подписчика
     * @param followeeId UUID идентификатор того, на кого подписываются
     * @return true если подписка оформлена успешно, false в противном случае
     */
    suspend fun follow(followerId: UUID, followeeId: UUID): Boolean

    /**
     * Отменяет подписку одного пользователя на другого.
     * @param followerId UUID идентификатор подписчика
     * @param followeeId UUID идентификатор того, на кого была подписка
     * @return true если отписка прошла успешно, false в противном случае
     */
    suspend fun unfollow(followerId: UUID, followeeId: UUID): Boolean

    /**
     * Получает список подписчиков пользователя с пагинацией.
     * @param userId UUID идентификатор пользователя
     * @param page Номер страницы (начиная с 0)
     * @param pageSize Количество элементов на странице
     * @return Ответ со списком подписчиков [FollowersResponse]
     */
    suspend fun listFollowers(userId: UUID, page: Int, pageSize: Int): FollowersResponse

    /**
     * Получает список подписок пользователя с пагинацией.
     * @param userId UUID идентификатор пользователя
     * @param page Номер страницы (начиная с 0)
     * @param pageSize Количество элементов на странице
     * @return Ответ со списком подписок [FollowingResponse]
     */
    suspend fun listFollowing(userId: UUID, page: Int, pageSize: Int): FollowingResponse
}