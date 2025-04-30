package com.mad.profile

import com.mad.profile.config.configureDependencyInjection
import com.mad.profile.routes.configureFollowerRoutes
import com.mad.profile.routes.configureProfileRoutes
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

/**
 * Точка входа в приложение Profile Service. Запускает встроенный Netty сервер
 */
fun main() {
  embeddedServer(
          Netty,
          port = System.getenv("PORT")?.toIntOrNull() ?: 8082,
          host = "0.0.0.0",
          module = Application::configureServer)
      .start(wait = true)
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

  configureDependencyInjection()

  install(ContentNegotiation) {
    json(
        Json {
          prettyPrint = true
          isLenient = true
          ignoreUnknownKeys = true
        })
  }

  routing {
    configureProfileRoutes()
    configureFollowerRoutes()
  }
}
