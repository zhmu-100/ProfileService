package com.mad.profile.repository

import com.mad.profile.model.ProfileEntity
import com.mad.profile.model.ProfileRequest
import java.util.UUID

/**
 * Интерфейс репозитория для работы с профилями пользователей.
 *
 * Определяет основные CRUD-операции для управления профилями,
 * а также дополнительные методы для получения статистики.
 */
interface ProfileRepository {
    /**
     * Создает новый профиль пользователя.
     *
     * @param profile Данные для создания профиля в формате [ProfileRequest]
     * @return Созданный [ProfileEntity]
     * @throws IllegalArgumentException если данные профиля невалидны
     */
    suspend fun create(profile: ProfileRequest): ProfileEntity

    /**
     * Получает профиль по уникальному идентификатору.
     *
     * @param id UUID искомого профиля
     * @return [ProfileEntity] если найден, иначе `null`
     */
    suspend fun getById(id: UUID): ProfileEntity?

    /**
     * Возвращает список профилей с пагинацией.
     *
     * @param page Номер страницы (начинается с 1)
     * @param pageSize Количество элементов на странице
     * @return [Pair] где:
     *         - первый элемент - список [ProfileEntity]
     *         - второй элемент - общее количество профилей
     */
    suspend fun list(page: Int, pageSize: Int): Pair<List<ProfileEntity>, Int>

    /**
     * Обновляет данные профиля.
     *
     * @param id UUID обновляемого профиля
     * @param profile Новые данные профиля в формате [ProfileRequest]
     * @return Обновленный [ProfileEntity] если профиль найден, иначе `null`
     */
    suspend fun update(id: UUID, profile: ProfileRequest): ProfileEntity?

    /**
     * Удаляет профиль пользователя.
     *
     * @param id UUID удаляемого профиля
     * @return `true` если профиль был удален, `false` если профиль не найден
     */
    suspend fun delete(id: UUID): Boolean

    /**
     * Получает количество подписчиков профиля.
     *
     * @param id UUID профиля
     * @return Количество подписчиков
     */
    suspend fun getFollowerCount(id: UUID): Int

    /**
     * Находит профиль по email пользователя.
     *
     * @param email Email искомого пользователя
     * @return [ProfileEntity] если найден, иначе `null`
     */
    suspend fun getByEmail(email: String): ProfileEntity?

    /**
     * Получает количество подписок профиля.
     *
     * @param id UUID профиля
     * @return Количество пользователей, на которых подписан данный профиль
     */
    suspend fun getFollowingCount(id: UUID): Int
}