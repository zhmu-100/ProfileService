package com.mad.profile.config

import com.mad.profile.actions.*
import com.mad.profile.service.ProfileService
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

/**
 * Конфигурация Dependency Injection для приложения.
 *
 * Устанавливает Koin как DI фреймворк и регистрирует все необходимые зависимости.
 *
 * @see Koin
 * @see IProfileAction
 * @see IFollowerAction
 */
fun Application.configureDependencyInjection() {
  install(Koin) {
    slf4jLogger()
    modules(appModule(this@configureDependencyInjection))
  }
}

/**
 * Модуль приложения для Koin
 *
 * Регистрирует все зависимости, необходимые для работы приложения
 *
 * @param app экземпляр [Application], используемый для получения конфигурации
 * @return модуль Koin с зарегистрированными зависимостями
 */
fun appModule(app: Application) = module {
  single { app.environment.config }
  single<IProfileAction> { ProfileAction(get()) }
  single<IFollowerAction> { FollowerAction(get()) }
  single { ProfileService(get(), get()) }
}
