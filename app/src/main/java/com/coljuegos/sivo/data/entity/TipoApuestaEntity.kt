package com.coljuegos.sivo.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "tipos_apuesta",
    indices = [Index(value = ["codigoTipoApuesta"], unique = true)]
)
data class TipoApuestaEntity(
    @PrimaryKey
    val uuidActa: UUID = UUID.randomUUID(),
    val codigoTipoApuesta: Long,
    val nombreTipoApuesta: String,
    val descripcionTipoApuesta: String
)