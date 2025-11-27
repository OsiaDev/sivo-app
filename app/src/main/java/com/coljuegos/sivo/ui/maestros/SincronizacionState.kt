package com.coljuegos.sivo.ui.maestros

/**
 * Estados de la sincronización de maestros
 */
sealed class SincronizacionState {
    object Idle : SincronizacionState()
    object Loading : SincronizacionState()
    data class Success(val message: String) : SincronizacionState()
    data class Error(val message: String) : SincronizacionState()
}