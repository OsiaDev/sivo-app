package com.coljuegos.sivo.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity(tableName = "actas",
    foreignKeys = [ForeignKey(entity = SessionEntity::class,
        parentColumns = arrayOf("uuidSession"),
        childColumns = arrayOf("uuidSession"))],
    indices = [
        Index(value = ["uuidActa"]),
        Index(value = ["uuidSession"]),
        Index(value = ["fechaVisitaAucActa"]),
        Index(value = ["tipoVisitaActa"]),
        Index(value = ["numActa"], unique = true),
        Index(value = ["uuidSession", "stateActa"]),
        Index(value = ["stateActa", "lastUpdatedActa"])
    ]
)
data class ActaEntity(
    @PrimaryKey
    val uuidActa: UUID = UUID.randomUUID(),
    val uuidSession: UUID,
    val numAucActa: Int,
    val fechaVisitaAucActa: LocalDate,
    val numActa: Int,
    val numContratoActa: String,
    val nitActa: String,
    val estCodigoActa: Int,
    val conCodigoActa: Int,
    val nombreOperadorActa: String,
    val fechaFinContratoActa: LocalDate,
    val emailActa: String,
    val tipoVisitaActa: String,
    val fechaCorteInventarioActa: LocalDateTime,
    val direccionActa: String,
    val establecimientoActa: String,
    val estCodigoInternoActa: String,
    val ciudadActa: String,
    val departamentoActa: String,
    val latitudActa: Double,
    val longitudActa: Double,
    val stateActa: ActaStateEnum = ActaStateEnum.ACTIVE,
    val lastUpdatedActa: LocalDateTime = LocalDateTime.now()
)
