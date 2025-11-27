package com.coljuegos.sivo.ui.establecimiento.firma

import android.graphics.Bitmap

data class FirmaActaUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // Fiscalizador Principal
    val nombreFiscalizadorPrincipal: String = "",
    val ccFiscalizadorPrincipal: String = "",
    val cargoFiscalizadorPrincipal: String = "",
    val firmaFiscalizadorPrincipal: Bitmap? = null,

    // Fiscalizador Secundario
    val nombreFiscalizadorSecundario: String = "",
    val ccFiscalizadorSecundario: String = "",
    val cargoFiscalizadorSecundario: String = "",
    val firmaFiscalizadorSecundario: Bitmap? = null,

    // Operador
    val nombreOperador: String = "",
    val ccOperador: String = "",
    val cargoOperador: String = "",
    val firmaOperador: Bitmap? = null,

    val isSaved: Boolean = false
)