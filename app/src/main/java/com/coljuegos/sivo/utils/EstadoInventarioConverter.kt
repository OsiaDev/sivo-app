package com.coljuegos.sivo.utils

import androidx.room.TypeConverter
import com.coljuegos.sivo.data.entity.EstadoInventarioEnum

class EstadoInventarioConverter {

    @TypeConverter
    fun fromEstadoInventario(estado: EstadoInventarioEnum): String {
        return estado.name
    }

    @TypeConverter
    fun toEstadoInventario(value: String): EstadoInventarioEnum {
        return try {
            EstadoInventarioEnum.valueOf(value)
        } catch (e: IllegalArgumentException) {
            EstadoInventarioEnum.OPERANDO
        }
    }

}