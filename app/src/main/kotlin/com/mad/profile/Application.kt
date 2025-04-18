package com.mad.profile

import com.mad.profile.config.AppConfig
import com.mad.profile.config.configureDatabases
import com.mad.profile.config.configureKoin
import com.mad.profile.routes.configureFollowerRoutes
import com.mad.profile.routes.configureProfileRoutes
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Точка входа в приложение Profile Service.
 * Запускает встроенный Netty сервер с конфигурацией из [AppConfig].
 *
 * @see AppConfig конфигурация хоста и порта сервера
 */
fun main() {
    embeddedServer(Netty, port = AppConfig.Server.port, host = AppConfig.Server.host) {
        configureServer()
    }.start(wait = true)
}

/**
 * Конфигурирует Ktor приложение для Profile Service.
 *
 * Выполняет следующие настройки:
 * 1. Настраивает dependency injection через Koin
 * 2. Конфигурирует подключение к базам данных
 * 3. Устанавливает ContentNegotiation для JSON сериализации
 * 4. Настраивает CORS политику
 * 5. Регистрирует все маршруты приложения
 */
fun Application.configureServer() {
    logger.info { "Starting Profile Service..." }

    /** Настраивает Koin для dependency injection */
    configureKoin()

    /** Конфигурирует подключения к базам данных */
    configureDatabases()

    /**
     * Настраивает контент-негосиацию для JSON.
     * Устанавливает следующие параметры:
     * - prettyPrint: форматированный вывод JSON
     * - isLenient: разрешает нестрогий синтаксис JSON
     * - ignoreUnknownKeys: игнорирует неизвестные ключи при десериализации
     */
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    /**
     * Настраивает CORS политику для всех хостов.
     * Разрешает стандартные HTTP методы и заголовки.
     */
    install(CORS) {
        anyHost()
        allowHeader("Content-Type")
        allowHeader("Authorization")
        allowMethod(io.ktor.http.HttpMethod.Options)
        allowMethod(io.ktor.http.HttpMethod.Get)
        allowMethod(io.ktor.http.HttpMethod.Post)
        allowMethod(io.ktor.http.HttpMethod.Put)
        allowMethod(io.ktor.http.HttpMethod.Delete)
    }

    /**
     * Регистрирует все маршруты приложения.
     * Включает маршруты для работы с профилями и подписчиками.
     */
    routing {
        configureProfileRoutes()
        configureFollowerRoutes()
    }
    
    logger.info { "Profile Service started on ${AppConfig.Server.host}:${AppConfig.Server.port}" }
}