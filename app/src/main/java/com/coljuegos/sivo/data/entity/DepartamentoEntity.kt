package com.coljuegos.sivo.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

@Entity(tableName = "departamentos",
    indices = [
        Index(value = ["uuidDepartamento"]),
        Index(value = ["nombreDepartamento"])
    ]
)
data class DepartamentoEntity(
    @PrimaryKey
    val uuidDepartamento: UUID = UUID.randomUUID(),
    val nombreDepartamento: String,
    val createdDepartamento: LocalDateTime = LocalDateTime.now()
)
