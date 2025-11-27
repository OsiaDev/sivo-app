package com.coljuegos.sivo.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "inventarios_registrados",
    foreignKeys = [
        ForeignKey(
            entity = ActaEntity::class,
            parentColumns = arrayOf("uuidActa"),
            childColumns = arrayOf("uuidActa"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = InventarioEntity::class,
            parentColumns = arrayOf("uuidInventario"),
            childColumns = arrayOf("uuidInventario"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["uuidInventarioRegistrado"]),
        Index(value = ["uuidActa"]),
        Index(value = ["uuidInventario"], unique = true)
    ]
)
data class InventarioRegistradoEntity(
    @PrimaryKey
    val uuidInventarioRegistrado: UUID = UUID.randomUUID(),
    val uuidActa: UUID,
    val uuidInventario: UUID,
    // Datos del formulario de registro
    val codigoApuestaDiferente: Boolean,
    val codigoApuestaDiferenteValor: String? = null,
    val serialVerificado: Boolean,
    val serialDiferente: String? = null,
    val descripcionJuego: Boolean,
    val planPremios: Boolean,
    val valorPremios: Boolean,
    val valorCredito: String? = null,

    // Contadores
    val contadoresVerificado: Boolean,

    // Contadores MET
    val coinInMet: String? = null,
    val coinOutMet: String? = null,
    val jackpotMet: String? = null,

    // Contadores SCLM
    val coinInSclm: String? = null,
    val coinOutSclm: String? = null,
    val jackpotSclm: String? = null,

    // Observaciones
    val observaciones: String? = null,
    val estado: EstadoInventarioEnum = EstadoInventarioEnum.OPERANDO
)