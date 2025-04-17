import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.21"
    id("org.jetbrains.dokka") version "1.9.10"
    kotlin("plugin.serialization") version "1.8.21"
    application
}

group = "com.mad"
version = "0.0.1"

application {
    mainClass.set("com.mad.profile.ApplicationKt")
}

repositories {
    mavenCentral()
}

val ktorVersion = "2.3.0"
val exposedVersion = "0.41.1"
val postgresVersion = "42.6.0"
val hikariVersion = "5.0.1"
val koinVersion = "3.4.1"
val logbackVersion = "1.4.8"
val kotlinLoggingVersion = "3.0.5"

dependencies {
    // Dependency Injection
    implementation("io.insert-koin:koin-core:$koinVersion")
    implementation("io.insert-koin:koin-ktor:$koinVersion")
    // Правильный артефакт для Koin Logger
    implementation("io.insert-koin:koin-logger-slf4j:$koinVersion")
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    
    // Ktor
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    
    // Database
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("com.zaxxer:HikariCP:$hikariVersion")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")
    
    // Testing
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.8.21")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    testImplementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    testImplementation("com.h2database:h2:2.1.214")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

// Увеличиваем память для Gradle
tasks.withType<JavaCompile> {
    options.compilerArgs = listOf("-Xmx2g")
}

// Настройка JVM аргументов для Gradle
tasks.withType<Test> {
    maxHeapSize = "2g"
}

// Настройка JVM аргументов для запуска приложения
tasks.withType<JavaExec> {
    jvmArgs = listOf("-Xmx2g")
}