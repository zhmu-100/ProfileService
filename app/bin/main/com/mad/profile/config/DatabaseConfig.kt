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

fun Application.configureDatabases() {
    val dataSource = createHikariDataSource()
    val database = Database.connect(dataSource)
    
     transaction(database) {
        SchemaUtils.create(Profiles, Followers)
        logger.info { "Database tables created or verified" }
    }
}

private fun createHikariDataSource(): DataSource {
    logger.info { "Configuring database connection: ${AppConfig.dbUrl}" }
    
    return HikariConfig().apply {
        driverClassName = "org.postgresql.Driver"
        jdbcUrl = AppConfig.dbUrl // Используем dbUrl для подключения
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


suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }