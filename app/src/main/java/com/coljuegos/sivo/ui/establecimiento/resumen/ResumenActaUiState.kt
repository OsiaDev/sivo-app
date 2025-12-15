package com.coljuegos.sivo.ui.establecimiento.resumen

import com.coljuegos.sivo.data.entity.ActaEntity
import com.coljuegos.sivo.data.entity.ActaStateEnum

data class ResumenActaUiState(
    val acta: ActaEntity? = null,
    val isLoading: Boolean = false,
    val isSincronizando: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val estadoActa: ActaStateEnum = ActaStateEnum.ACTIVE,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val ubicacionObtenida: Boolean = false,
    val debeNavegarAlHome: Boolean = false
)