package com.coljuegos.sivo.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "verificacion_juego_responsable",
    foreignKeys = [
        ForeignKey(
            entity = ActaEntity::class,
            parentColumns = arrayOf("uuidActa"),
            childColumns = arrayOf("uuidActa"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["uuidVerificacionJuegoResponsable"]),
        Index(value = ["uuidActa"], unique = true)
    ]
)
data class VerificacionJuegoResponsableEntity(
    @PrimaryKey
    val uuidVerificacionJuegoResponsable: UUID = UUID.randomUUID(),
    val uuidActa: UUID,
    val cuentaTestIdentificacionRiesgos: String? = null,   // Si/No/NA
    val existenPiezasPublicitarias: String? = null,         // Si/No/NA
    val cuentaProgramaJuegoResponsable: String? = null      // Si/No/NA
)