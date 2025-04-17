package com.mad.profile.model

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

object Profiles : UUIDTable("profiles") {
    val name: Column<String> = varchar("name", 255)
    val email: Column<String> = varchar("email", 255).uniqueIndex()
    val imageId: Column<String?> = varchar("image_id", 255).nullable()
    val bio: Column<String?> = text("bio").nullable()
    val country: Column<String?> = varchar("country", 100).nullable()
    val city: Column<String?> = varchar("city", 100).nullable()
    val birthdate: Column<LocalDate?> = date("birthdate").nullable()
    val weight: Column<Double?> = double("weight").nullable()
    val height: Column<Double?> = double("height").nullable()
    val createdAt: Column<Instant> = timestamp("created_at").default(Instant.now())
    val updatedAt: Column<Instant> = timestamp("updated_at").default(Instant.now())
}

object Followers : Table("followers") {
    val followerId = reference("follower_id", Profiles)
    val followeeId = reference("followee_id", Profiles)
    // Изменяем тип на Instant
    val createdAt = timestamp("created_at").default(Instant.now())
    
    override val primaryKey = PrimaryKey(followerId, followeeId)
}