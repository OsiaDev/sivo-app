package com.coljuegos.sivo.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "inventarios_bingo_registrados",
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
        Index(value = ["uuidInventarioBingoRegistrado"]),
        Index(value = ["uuidActa"]),
        Index(value = ["uuidInventario"], unique = true)
    ]
)
data class InventarioBingoRegistradoEntity(
    @PrimaryKey
    val uuidInventarioBingoRegistrado: UUID = UUID.randomUUID(),
    val uuidActa: UUID,
    val uuidInventario: UUID,
    val codigoApuestaDiferente: Boolean = false,
    val codigoApuestaDiferenteValor: String? = null,
    val sillasDiferente: Boolean = false,
    val sillasValor: Int? = null,
    val estado: EstadoInventarioEnum = EstadoInventarioEnum.OPERANDO,
    val observaciones: String? = null
)