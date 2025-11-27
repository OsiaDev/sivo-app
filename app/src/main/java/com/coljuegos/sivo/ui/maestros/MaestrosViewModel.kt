package com.coljuegos.sivo.ui.maestros

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.repository.MaestrosRepository
import com.coljuegos.sivo.utils.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaestrosViewModel @Inject constructor(
    private val maestrosRepository: MaestrosRepository
) : ViewModel() {

    private val _sincronizacionState = MutableStateFlow<SincronizacionState>(SincronizacionState.Idle)
    val sincronizacionState: StateFlow<SincronizacionState> = _sincronizacionState.asStateFlow()

    /**
     * Sincroniza los maestros desde el servidor
     */
    fun sincronizarMaestros() {
        viewModelScope.launch {
            maestrosRepository.sincronizarMaestros().collect { result ->
                _sincronizacionState.value = when (result) {
                    is NetworkResult.Loading -> SincronizacionState.Loading
                    is NetworkResult.Success -> SincronizacionState.Success("Maestros sincronizados correctamente")
                    is NetworkResult.Error -> SincronizacionState.Error(result.message ?: "Error desconocido")
                }
            }
        }
    }

    /**
     * Fuerza la actualización de maestros eliminando los datos locales
     */
    fun forzarActualizacion() {
        viewModelScope.launch {
            maestrosRepository.forzarActualizacion().collect { result ->
                _sincronizacionState.value = when (result) {
                    is NetworkResult.Loading -> SincronizacionState.Loading
                    is NetworkResult.Success -> SincronizacionState.Success("Maestros actualizados correctamente")
                    is NetworkResult.Error -> SincronizacionState.Error(result.message ?: "Error al actualizar")
                }
            }
        }
    }

    /**
     * Verifica si necesita sincronización
     */
    suspend fun necesitaSincronizacion(): Boolean {
        return maestrosRepository.necesitaSincronizacion()
    }

    /**
     * Obtiene la cantidad de tipos de apuesta
     */
    suspend fun getCantidadTiposApuesta(): Int {
        return maestrosRepository.getCantidadTiposApuesta()
    }

    /**
     * Resetea el estado de sincronización
     */
    fun resetState() {
        _sincronizacionState.value = SincronizacionState.Idle
    }
}