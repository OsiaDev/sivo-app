package com.coljuegos.sivo.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

@Entity(
    tableName = "imagenes",
    foreignKeys = [ForeignKey(
        entity = ActaEntity::class,
        parentColumns = arrayOf("uuidActa"),
        childColumns = arrayOf("uuidActa"),
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index(value = ["uuidImagen"]),
        Index(value = ["uuidActa"]),
        Index(value = ["fechaCaptura"])
    ]
)
data class ImagenEntity(
    @PrimaryKey
    val uuidImagen: UUID = UUID.randomUUID(),
    val uuidActa: UUID,
    val rutaImagen: String,
    val nombreImagen: String,
    val fechaCaptura: LocalDateTime = LocalDateTime.now(),
    val descripcion: String? = null,
    val tamanoBytesImagen: Long = 0L,
    val fragmentOrigen: String? = null
)