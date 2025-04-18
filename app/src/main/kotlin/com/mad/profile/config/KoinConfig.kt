package com.mad.profile.config

import com.mad.profile.repository.FollowerRepository
import com.mad.profile.repository.ProfileRepository
import com.mad.profile.repository.impl.FollowerRepositoryImpl
import com.mad.profile.repository.impl.ProfileRepositoryImpl
import com.mad.profile.service.ProfileService
import com.mad.profile.service.impl.ProfileServiceImpl
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.SLF4JLogger

/**
 * Настройка DI-контейнера Koin для приложения.
 *
 * Этот модуль конфигурирует зависимости приложения с использованием Koin.
 */
fun Application.configureKoin() {
    install(Koin) {
        // Используем правильный класс Slf4jLogger
        SLF4JLogger()
        modules(appModule)
    }
}

/**
 * Основной модуль зависимостей приложения.
 *
 * Содержит все зарегистрированные зависимости:
 * - Репозитории
 * - Сервисы
 *
 * Модуль используется Koin для внедрения зависимостей.
 */
val appModule = module {

    /**
     * Регистрация реализации [ProfileRepository].
     *
     * @return Экземпляр [ProfileRepositoryImpl] как синглтон
     */
    single<ProfileRepository> { ProfileRepositoryImpl() }

    /**
     * Регистрация реализации [FollowerRepository].
     *
     * @return Экземпляр [FollowerRepositoryImpl] как синглтон
     */
    single<FollowerRepository> { FollowerRepositoryImpl() }

    /**
     * Регистрация реализации [ProfileService].
     *
     * Автоматически внедряет зависимости:
     * - [ProfileRepository]
     * - [FollowerRepository]
     *
     * @return Экземпляр [ProfileServiceImpl] как синглтон
     */
    single<ProfileService> { ProfileServiceImpl(get(), get()) }
}