package com.coljuegos.sivo.ui.establecimiento.novedad

import com.coljuegos.sivo.data.entity.NovedadRegistradaEntity

data class RegistrarNovedadUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val novedadRegistrada: NovedadRegistradaEntity? = null,
    val esEdicion: Boolean = false,
    val guardadoExitoso: Boolean = false
)