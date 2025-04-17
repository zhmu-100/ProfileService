package com.mad.profile.config

object AppConfig {
    object Server {
        val host: String = System.getenv("HOST") ?: "0.0.0.0"
        val port: Int = System.getenv("PORT")?.toIntOrNull() ?: 8081
    }

    object Database {
        // Чтение dbMode из переменных окружения или конфигурации
        val mode: String = System.getenv("DB_MODE") ?: "LOCAL" // если не задано, будет "LOCAL"

        // В зависимости от режима задаем параметры базы данных
        val host: String = System.getenv("DB_HOST") ?: if (mode == "GATEWAY") "gateway-host" else "localhost"
        val port: Int = System.getenv("DB_PORT")?.toIntOrNull() ?: if (mode == "GATEWAY") 8080 else 5432
        val name: String = System.getenv("DB_NAME") ?: "postgres"
        val user: String = System.getenv("DB_USER") ?: "postgres"
        val password: String = System.getenv("DB_PASSWORD") ?: "postgres"
        val maxPoolSize: Int = System.getenv("DB_MAX_POOL_SIZE")?.toIntOrNull() ?: 10
    }

    // В зависимости от режима конфигурируем URL для доступа к базе
    val dbUrl: String
        get() = if (Database.mode == "GATEWAY") {
            // Если режим "GATEWAY", формируем URL для подключения через API
            "http://${Database.host}:${Database.port}/api/db"
        } else {
            // Если режим "LOCAL", используем стандартный JDBC URL для подключения к базе данных
            "jdbc:postgresql://${Database.host}:${Database.port}/${Database.name}"
        }
}
