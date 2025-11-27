package com.coljuegos.sivo.ui.establecimiento.novedad

data class NovedadReportadaUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val novedadesRegistradas: List<NovedadConRegistro> = emptyList(),
    val totalNovedadesRegistradas: Int = 0
)