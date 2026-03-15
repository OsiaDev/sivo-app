package com.coljuegos.sivo.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "resumen_inventario",
    foreignKeys = [ForeignKey(
        entity = ActaEntity::class,
        parentColumns = ["uuidActa"],
        childColumns = ["uuidActa"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["uuidActa"], unique = true)]
)
data class ResumenInventarioEntity(
    @PrimaryKey
    val uuidResumen: UUID = UUID.randomUUID(),
    val uuidActa: UUID,
    val notasResumen: String = "",
    val observacionesOperador: String? = null
)