package com.coljuegos.sivo.ui.establecimiento.resumen

import com.coljuegos.sivo.data.entity.ActaEntity
import com.coljuegos.sivo.data.entity.ActaStateEnum

data class ResumenActaUiState(
    val isLoading: Boolean = false,
    val acta: ActaEntity? = null,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val estadoActa: ActaStateEnum = ActaStateEnum.ACTIVE,
    val ubicacionObtenida: Boolean = false,
    val isSincronizando: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)