package com.coljuegos.sivo.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "verificacion_contractual",
    foreignKeys = [
        ForeignKey(
            entity = ActaEntity::class,
            parentColumns = arrayOf("uuidActa"),
            childColumns = arrayOf("uuidActa"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["uuidVerificacionContractual"]),
        Index(value = ["uuidActa"], unique = true)
    ]
)
data class VerificacionContractualEntity(
    @PrimaryKey
    val uuidVerificacionContractual: UUID = UUID.randomUUID(),
    val uuidActa: UUID,
    val avisoAutorizacion: String? = null, // Si/No/NA
    val direccionCorresponde: String? = null, // Si/No/NA
    val otraDireccion: String? = null,
    val nombreEstablecimientoCorresponde: String? = null, // Si/No/NA
    val otroNombre: String? = null,
    val desarrollaActividadesDiferentes: String? = null, // Si/No/NA
    val tipoActividad: String? = null, // Bar/Billares/Tienda/Otros
    val especificacionOtros: String? = null, // Texto libre para "Otros"
    val cuentaRegistrosMantenimiento: String? = null // Si/No/NA
)