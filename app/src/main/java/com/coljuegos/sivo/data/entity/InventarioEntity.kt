package com.coljuegos.sivo.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "inventarios",
    foreignKeys = [ForeignKey(entity = ActaEntity::class,
        parentColumns = arrayOf("uuidActa"),
        childColumns = arrayOf("uuidActa"))],
    indices = [
        Index(value = ["uuidInventario"]),
        Index(value = ["uuidActa"])
    ]
)
data class InventarioEntity(
    @PrimaryKey
    val uuidInventario: UUID = UUID.randomUUID(),
    val uuidActa: UUID,
    val nombreMarcaInventario: String,
    val metSerialInventario: String,
    val insCodigoInventario: String,
    val invSillasInventario: Int,
    val tipoApuestaNombreInventario: String,
    val metOnlineInventario: Boolean,
    val codigoTipoApuestaInventario: String,
    val nucInventario: String,
    val conCodigoInventario: Int,
    val aucNumeroInventario: Int,
    val estCodigoInventario: Int
)
