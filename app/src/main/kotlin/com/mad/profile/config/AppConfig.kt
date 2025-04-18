package com.mad.profile.config

/**
 * Основной объект конфигурации приложения.
 *
 * Содержит настройки для:
 * - Сервера (хост и порт)
 * - Базы данных (параметры подключения)
 * - Режимов работы приложения
 *
 * Все параметры могут быть переопределены через переменные окружения.
 */
object AppConfig {
    /**
     * Конфигурация сервера приложения.
     */
    object Server {
        /**
         * Хост для запуска сервера.
         * По умолчанию: "0.0.0.0" (принимать соединения со всех интерфейсов)
         * Может быть переопределен через переменную окружения HOST.
         */
        val host: String = System.getenv("HOST") ?: "0.0.0.0"

        /**
         * Порт для запуска сервера.
         * По умолчанию: 8081
         * Может быть переопределен через переменную окружения PORT.
         */
        val port: Int = System.getenv("PORT")?.toIntOrNull() ?: 8081
    }

    /**
     * Конфигурация подключения к базе данных.
     */
    object Database {
        /**
         * Режим работы с базой данных.
         * Возможные значения:
         * - "LOCAL" - прямое подключение к СУБД
         * - "GATEWAY" - подключение через API шлюз
         * По умолчанию: "LOCAL"
         * Может быть переопределен через переменную окружения DB_MODE.
         */
        val mode: String = System.getenv("DB_MODE") ?: "LOCAL"

        /**
         * Хост базы данных.
         * Для режима GATEWAY по умолчанию: "gateway-host"
         * Для режима LOCAL по умолчанию: "localhost"
         * Может быть переопределен через переменную окружения DB_HOST.
         */
        val host: String = System.getenv("DB_HOST") ?: if (mode == "GATEWAY") "gateway-host" else "localhost"

        /**
         * Порт базы данных.
         * Для режима GATEWAY по умолчанию: 8080
         * Для режима LOCAL по умолчанию: 5432 (стандартный порт PostgreSQL)
         * Может быть переопределен через переменную окружения DB_PORT.
         */
        val port: Int = System.getenv("DB_PORT")?.toIntOrNull() ?: if (mode == "GATEWAY") 8080 else 5432

        /**
         * Имя базы данных.
         * По умолчанию: "postgres"
         * Может быть переопределен через переменную окружения DB_NAME.
         */
        val name: String = System.getenv("DB_NAME") ?: "postgres"

        /**
         * Пользователь для подключения к базе данных.
         * По умолчанию: "postgres"
         * Может быть переопределен через переменную окружения DB_USER.
         */
        val user: String = System.getenv("DB_USER") ?: "postgres"

        /**
         * Пароль для подключения к базе данных.
         * По умолчанию: "postgres"
         * Может быть переопределен через переменную окружения DB_PASSWORD.
         */
        val password: String = System.getenv("DB_PASSWORD") ?: "postgres"

        /**
         * Максимальный размер пула соединений.
         * По умолчанию: 10
         * Может быть переопределен через переменную окружения DB_MAX_POOL_SIZE.
         */
        val maxPoolSize: Int = System.getenv("DB_MAX_POOL_SIZE")?.toIntOrNull() ?: 10
    }

    /**
     * URL для подключения к базе данных.
     *
     * Формат зависит от режима работы:
     * - В режиме GATEWAY: HTTP URL для доступа через API
     * - В режиме LOCAL: JDBC URL для прямого подключения к PostgreSQL
     *
     * @return Строка подключения к базе данных
     */
    val dbUrl: String
        get() = if (Database.mode == "GATEWAY") {
            "http://${Database.host}:${Database.port}/api/db"
        } else {
            "jdbc:postgresql://${Database.host}:${Database.port}/${Database.name}"
        }
}