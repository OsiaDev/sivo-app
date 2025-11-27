package com.coljuegos.sivo.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "verificacion_siplaft",
    foreignKeys = [
        ForeignKey(
            entity = ActaEntity::class,
            parentColumns = arrayOf("uuidActa"),
            childColumns = arrayOf("uuidActa"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["uuidVerificacionSiplaft"]),
        Index(value = ["uuidActa"], unique = true)
    ]
)
data class VerificacionSiplaftEntity(
    @PrimaryKey
    val uuidVerificacionSiplaft: UUID = UUID.randomUUID(),
    val uuidActa: UUID,
    val cuentaFormatoIdentificacion: String? = null, // Si/No/NA
    val montoIdentificacion: String? = null, // Texto libre cuando es "Si"
    val cuentaFormatoReporteInterno: String? = null, // Si/No/NA
    val senalesAlerta: String? = null, // Texto libre cuando es "Si"
    val conoceCodigoConducta: String? = null // Si/No/NA
)