package com.coljuegos.sivo.data.entity

enum class EstadoInventarioEnum {
    OPERANDO,
    APAGADO,
    NO_ENCONTRADO;

    companion object {
        fun fromString(value: String): EstadoInventarioEnum {
            return when (value) {
                "Operando" -> OPERANDO
                "Apagado" -> APAGADO
                "No encontrado" -> NO_ENCONTRADO
                else -> OPERANDO
            }
        }

        fun toString(estado: EstadoInventarioEnum): String {
            return when (estado) {
                OPERANDO -> "Operando"
                APAGADO -> "Apagado"
                NO_ENCONTRADO -> "No encontrado"
            }
        }
    }

}