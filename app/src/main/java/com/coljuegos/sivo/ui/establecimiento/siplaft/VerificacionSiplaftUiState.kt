package com.coljuegos.sivo.ui.establecimiento.siplaft

import java.util.UUID

data class VerificacionSiplaftUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val actaUuid: UUID? = null,
    val cuentaFormatoIdentificacion: String = "",
    val montoIdentificacion: String = "",
    val cuentaFormatoReporteInterno: String = "",
    val senalesAlerta: String = "",
    val conoceCodigoConducta: String = "",
    val mostrarCampoMonto: Boolean = false,
    val mostrarCampoSenales: Boolean = false
)