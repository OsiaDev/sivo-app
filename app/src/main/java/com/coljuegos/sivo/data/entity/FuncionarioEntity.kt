package com.coljuegos.sivo.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "funcionarios",
    foreignKeys = [ForeignKey(entity = ActaEntity::class,
        parentColumns = arrayOf("uuidActa"),
        childColumns = arrayOf("uuidActa"))],
    indices = [
        Index(value = ["uuidFuncionario"]),
        Index(value = ["uuidActa"])
    ]
)
data class FuncionarioEntity(
    @PrimaryKey
    val uuidFuncionario: UUID = UUID.randomUUID(),
    val uuidActa: UUID,
    val idUsuarioFuncionario: String,
    val nombreFuncionario: String,
    val cargoFuncionario: String,
    val emailFuncionario: String,
    val identificacionFuncionario: String
)