package com.coljuegos.sivo.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

@Entity(tableName = "municipios",
    foreignKeys = [ForeignKey(entity = DepartamentoEntity::class,
        parentColumns = arrayOf("uuidDepartamento"),
        childColumns = arrayOf("uuidDepartamento"))],
    indices = [
        Index(value = ["uuidMunicipio"]),
        Index(value = ["uuidDepartamento"]),
        Index(value = ["nombreMunicipio"]),
        Index(value = ["uuidDepartamento", "nombreMunicipio"], unique = true)
    ]
)
data class MunicipioEntity(
    @PrimaryKey
    val uuidMunicipio: UUID = UUID.randomUUID(),
    val uuidDepartamento: UUID,
    val nombreMunicipio: String,
    val createdMunicipio: LocalDateTime = LocalDateTime.now()
)
