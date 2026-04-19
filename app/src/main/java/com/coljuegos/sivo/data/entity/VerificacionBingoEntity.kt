package com.coljuegos.sivo.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "verificacion_bingo",
    foreignKeys = [
        ForeignKey(
            entity = ActaEntity::class,
            parentColumns = arrayOf("uuidActa"),
            childColumns = arrayOf("uuidActa"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["uuidVerificacionBingo"]),
        Index(value = ["uuidActa"], unique = true)
    ]
)
data class VerificacionBingoEntity(
    @PrimaryKey
    val uuidVerificacionBingo: UUID = UUID.randomUUID(),
    val uuidActa: UUID,
    val cartonesModulos: String? = null,           // Texto libre
    val sistemaTecnologico: String? = null,         // Si/No/NA
    val sistemaInterconectado: String? = null,       // Si/No/NA
    val realizaEventosEspeciales: String? = null,   // Si/No/NA
    val tipoBalotera: String? = null,               // Manual / Electroneumática
    val valorCartonExpuesto: String? = null         // Si/No/NA
)