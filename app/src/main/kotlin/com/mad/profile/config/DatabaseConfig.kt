package com.mad.profile.config

import com.mad.profile.model.Followers
import com.mad.profile.model.Profiles
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

private val logger = KotlinLogging.logger {}

/**
 * Конфигурация подключения к базе данных приложения.
 *
 * Инициализирует соединение с базой данных, создает таблицы (если они не существуют)
 * и настраивает пул соединений через HikariCP.
 */
fun Application.configureDatabases() {
    val dataSource = createHikariDataSource()
    val database = Database.connect(dataSource)

    transaction(database) {
        SchemaUtils.create(Profiles, Followers)
        logger.info { "Database tables created or verified" }
    }
}

/**
 * Создает и настраивает DataSource с использованием HikariCP.
 *
 * @return Настроенный [DataSource] с пулом соединений
 * @see HikariConfig
 * @see HikariDataSource
 *
 * Конфигурация включает:
 * - Драйвер PostgreSQL
 * - URL подключения из [AppConfig]
 * - Учетные данные из [AppConfig.Database]
 * - Настройки пула соединений
 * - Уровень изоляции транзакций
 */
private fun createHikariDataSource(): DataSource {
    logger.info { "Configuring database connection: ${AppConfig.dbUrl}" }

    return HikariConfig().apply {
        driverClassName = "org.postgresql.Driver"
        jdbcUrl = AppConfig.dbUrl
        username = AppConfig.Database.user
        password = AppConfig.Database.password
        maximumPoolSize = AppConfig.Database.maxPoolSize
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }.let { config ->
        HikariDataSource(config)
    }
}

/**
 * Выполняет запрос к базе данных в корутине с использованием IO-диспетчера.
 *
 * @param block Блок кода, содержащий операции с базой данных
 * @return Результат выполнения блока кода
 * @throws Exception В случае ошибки при выполнении запроса
 *
 * Использует [newSuspendedTransaction] для выполнения в транзакции
 * с автоматическим управлением соединением.
 */
suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }