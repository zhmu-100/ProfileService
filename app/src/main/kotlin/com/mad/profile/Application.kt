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

fun main() {
    embeddedServer(Netty, port = AppConfig.Server.port, host = AppConfig.Server.host) {
        configureServer()
    }.start(wait = true)
}

fun Application.configureServer() {
    logger.info { "Starting Profile Service..." }
    
    // Configure Koin for dependency injection
    configureKoin()
    
    configureDatabases()
    
    // Configure content negotiation with JSON
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    
    // Configure CORS
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
    
    // Configure routes
    routing {
        configureProfileRoutes()
        configureFollowerRoutes()
    }
    
    logger.info { "Profile Service started on ${AppConfig.Server.host}:${AppConfig.Server.port}" }
}