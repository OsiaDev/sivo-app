package com.coljuegos.sivo.data.entity

data class MunicipioDisplayItem(
    val municipioId: String,
    val municipioNombre: String,
    val departamentoNombre: String
) {
    val displayName: String
        get() = "$municipioNombre, $departamentoNombre"
}