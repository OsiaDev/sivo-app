package com.coljuegos.sivo.data.entity

enum class ApuestaCodigoEnum(val codigos: Array<String>, val nombre: String) {
    MET(arrayOf("1", "2", "3"), "MET"),
    MESAS(arrayOf("4"), "MESAS"),
    BINGO(arrayOf("6", "7", "8", "9", "10", "11", "12", "13"), "BINGO"),
    OTROS(arrayOf("5", "14"), "OTROS");

    companion object {
        fun fromCodigo(codigo: String): ApuestaCodigoEnum {
            return entries.firstOrNull { it.codigos.contains(codigo) } ?: MET
        }

    }

}