package com.coljuegos.sivo.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

@Entity(tableName = "sessions",
    indices = [
        Index(value = ["uuidSession"]),
        Index(value = ["idUserSession"])
    ]
)
data class SessionEntity (
    @PrimaryKey
    val uuidSession: UUID = UUID.randomUUID(),
    val tokenSession: String,
    val tokenTypeSession: String,
    val usernameSession: String,
    val idUserSession: String,
    val nameUserSession: String,
    val fullNameUserSession: String,
    val emailUserSession: String,
    val perCodigoSession: Long,
    val expirationSession: LocalDateTime,
    val isAuthSession: Boolean = true,
    val lastSession: LocalDateTime = LocalDateTime.now()
)
