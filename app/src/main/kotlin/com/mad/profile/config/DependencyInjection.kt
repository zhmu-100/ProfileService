package com.mad.profile.config

import com.mad.profile.actions.*
import com.mad.profile.service.ProfileService
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureDependencyInjection() {
  install(Koin) {
    slf4jLogger()
    modules(appModule(this@configureDependencyInjection))
  }
}

fun appModule(app: Application) = module {
  single { app.environment.config }
  single<IProfileAction> { ProfileAction(get()) }
  single<IFollowerAction> { FollowerAction(get()) }
  single { ProfileService(get(), get()) }
}
