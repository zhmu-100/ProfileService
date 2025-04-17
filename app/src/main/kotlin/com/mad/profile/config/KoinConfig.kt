package com.mad.profile.config

import com.mad.profile.repository.FollowerRepository
import com.mad.profile.repository.ProfileRepository
import com.mad.profile.repository.impl.FollowerRepositoryImpl
import com.mad.profile.repository.impl.ProfileRepositoryImpl
import com.mad.profile.service.ProfileService
import com.mad.profile.service.impl.ProfileServiceImpl
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.SLF4JLogger

fun Application.configureKoin() {
    install(Koin) {
        // Используем правильный класс Slf4jLogger
        SLF4JLogger()
        modules(appModule)
    }
}

val appModule = module {
    // Repositories
    single<ProfileRepository> { ProfileRepositoryImpl() }
    single<FollowerRepository> { FollowerRepositoryImpl() }
    
    // Services
    single<ProfileService> { ProfileServiceImpl(get(), get()) }
}