package com.mad.profile

import com.mad.profile.config.configureDependencyInjection
import com.mad.profile.logging.LoggerProvider
import com.mad.profile.routes.configureFollowerRoutes
import com.mad.profile.routes.configureProfileRoutes
import io.github.cdimascio.dotenv.dotenv
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

/** Точка входа в приложение Profile Service. Запускает встроенный Netty сервер */
fun main() {
  val logger = LoggerProvider.logger
  val dotenv = dotenv()
  val port = dotenv["PORT"]?.toIntOrNull() ?: 8002

  logger.logActivity(
      "Запуск Profile Service",
      additionalData = mapOf("port" to port.toString(), "host" to "0.0.0.0"))

  try {
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::configureServer)
        .start(wait = true)

    logger.logActivity("Profile Service успешно запущен")
  } catch (e: Exception) {
    logger.logError(
        "Ошибка при запуске Profile Service",
        errorMessage = e.message ?: "Неизвестная ошибка",
        stackTrace = e.stackTraceToString())
    throw e
  }
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
  val logger = LoggerProvider.logger

  logger.logActivity("Настройка модулей приложения")

  try {
    configureDependencyInjection()

    logger.logActivity("Dependency injection настроен")

    install(ContentNegotiation) {
      json(
          Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
          })
    }

    logger.logActivity("Content negotiation настроен")

    routing {
      configureProfileRoutes()
      configureFollowerRoutes()
    }

    logger.logActivity("Маршруты настроены")

    environment.monitor.subscribe(ApplicationStopped) {
      logger.logActivity("Остановка Profile Service")
      logger.close()
    }

    logger.logActivity("Модули приложения успешно настроены")
  } catch (e: Exception) {
    logger.logError(
        "Ошибка при настройке модулей приложения",
        errorMessage = e.message ?: "Неизвестная ошибка",
        stackTrace = e.stackTraceToString())
    throw e
  }
}
