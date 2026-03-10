package com.coljuegos.sivo.ui.establecimiento.juegoresponsable

import java.util.UUID

data class VerificacionJuegoResponsableUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val actaUuid: UUID? = null,
    val cuentaTestIdentificacionRiesgos: String = "",
    val existenPiezasPublicitarias: String = "",
    val cuentaProgramaJuegoResponsable: String = ""
)