package com.coljuegos.sivo.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "actasvisitas",
    foreignKeys = [
        ForeignKey(
            entity = ActaEntity::class,
            parentColumns = arrayOf("uuidActa"),
            childColumns = arrayOf("uuidActa")
        ),
        ForeignKey(
            entity = MunicipioEntity::class,
            parentColumns = arrayOf("uuidMunicipio"),
            childColumns = arrayOf("uuidMunicipio")
        )
    ],
    indices = [
        Index(value = ["uuidActaVisita"]),
        Index(value = ["uuidActa"], unique = true),
        Index(value = ["uuidMunicipio"])
    ]
)
data class ActaVisitaEntity(
    @PrimaryKey
    val uuidActaVisita: UUID = UUID.randomUUID(),
    val uuidActa: UUID,
    val nombrePresente: String? = null,
    val identificacionPresente: String? = null,
    val uuidMunicipio: UUID? = null,
    val cargoPresente: String? = null,
    val emailPresente: String? = null,
    val correosContacto: String? = null  // NUEVO CAMPO - String con delimitador ";"
)
