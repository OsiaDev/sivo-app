package com.coljuegos.sivo.ui.establecimiento.bingo

import java.util.UUID

data class VerificacionBingoUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val actaUuid: UUID? = null,
    val tieneBingo: Boolean = false,
    // Preguntas globales
    val cartonesModulos: String = "",
    val sistemaTecnologico: String = "",
    val sistemaInterconectado: String = "",
    val realizaEventosEspeciales: String = "",
    val tipoBalotera: String = "",
    val valorCartonExpuesto: String = ""
)