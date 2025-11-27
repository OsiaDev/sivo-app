package com.coljuegos.sivo.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "firma_acta",
    foreignKeys = [
        ForeignKey(
            entity = ActaEntity::class,
            parentColumns = arrayOf("uuidActa"),
            childColumns = arrayOf("uuidActa"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["uuidFirmaActa"]),
        Index(value = ["uuidActa"], unique = true)
    ]
)
data class FirmaActaEntity(
    @PrimaryKey
    val uuidFirmaActa: UUID = UUID.randomUUID(),
    val uuidActa: UUID,

    // Fiscalizador Principal
    val nombreFiscalizadorPrincipal: String? = null,
    val ccFiscalizadorPrincipal: String? = null,
    val cargoFiscalizadorPrincipal: String? = null,
    val firmaFiscalizadorPrincipal: String? = null, // Base64 encoded bitmap

    // Fiscalizador Acompañante (Secundario)
    val nombreFiscalizadorSecundario: String? = null,
    val ccFiscalizadorSecundario: String? = null,
    val cargoFiscalizadorSecundario: String? = null,
    val firmaFiscalizadorSecundario: String? = null, // Base64 encoded bitmap

    // Operador
    val nombreOperador: String? = null,
    val ccOperador: String? = null,
    val cargoOperador: String? = null,
    val firmaOperador: String? = null // Base64 encoded bitmap
)