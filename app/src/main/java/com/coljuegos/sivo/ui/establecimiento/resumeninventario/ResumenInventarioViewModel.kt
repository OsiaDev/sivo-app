package com.coljuegos.sivo.ui.establecimiento.resumeninventario

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.dao.InventarioRegistradoDao
import com.coljuegos.sivo.data.dao.NovedadRegistradaDao
import com.coljuegos.sivo.data.dao.ResumenInventarioDao
import com.coljuegos.sivo.data.entity.EstadoInventarioEnum
import com.coljuegos.sivo.data.entity.ResumenInventarioEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ResumenInventarioViewModel @Inject constructor(
    private val inventarioRegistradoDao: InventarioRegistradoDao,
    private val novedadRegistradaDao: NovedadRegistradaDao,
    private val resumenInventarioDao: ResumenInventarioDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val actaUuid: UUID = checkNotNull(savedStateHandle.get<UUID>("actaUuid"))

    private val _uiState = MutableStateFlow(ResumenInventarioUiState())
    val uiState: StateFlow<ResumenInventarioUiState> = _uiState.asStateFlow()


    init {
        calcularEstadisticas()
        cargarNotas()
    }

    private fun calcularEstadisticas() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                // Obtener inventarios registrados
                inventarioRegistradoDao.getInventariosRegistradosByActa(actaUuid).collect { inventariosRegistrados ->

                    // Obtener novedades registradas
                    val novedadesRegistradas = novedadRegistradaDao.getNovedadesRegistradasByActaList(actaUuid)

                    // 1. Inventarios registrados operando o apagado
                    val inventariosOperandoApagado = inventariosRegistrados.count {
                        it.estado == EstadoInventarioEnum.OPERANDO ||
                                it.estado == EstadoInventarioEnum.APAGADO
                    }

                    // 2. Inventarios no encontrados
                    val inventariosNoEncontrados = inventariosRegistrados.count {
                        it.estado == EstadoInventarioEnum.NO_ENCONTRADO
                    }

                    // 3. Novedades sin placa
                    val novedadesSinPlaca = novedadesRegistradas.count { !it.tienePlaca }

                    // 4. Novedades con placa (total novedades - sin placa)
                    val novedadesConPlaca = novedadesRegistradas.size - novedadesSinPlaca

                    // 5. Total inventarios encontrados: (1 - 2 + 3 + 4)
                    val totalInventariosEncontrados = inventariosOperandoApagado -
                            inventariosNoEncontrados +
                            novedadesSinPlaca +
                            novedadesConPlaca

                    // 6. Inventarios con código de apuesta diferente
                    val codigoApuestaDiferente = inventariosRegistrados.count {
                        it.codigoApuestaDiferente
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            inventariosOperandoApagado = inventariosOperandoApagado,
                            inventariosNoEncontrados = inventariosNoEncontrados,
                            novedadesSinPlaca = novedadesSinPlaca,
                            novedadesConPlaca = novedadesConPlaca,
                            totalInventariosEncontrados = totalInventariosEncontrados,
                            codigoApuestaDiferente = codigoApuestaDiferente,
                            errorMessage = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al calcular estadísticas: ${e.message}"
                    )
                }
            }
        }
    }

    private fun cargarNotas() {
        viewModelScope.launch {
            val resumen = resumenInventarioDao.getResumenByActaId(actaUuid)
            _uiState.update { 
                it.copy(
                    notas = resumen?.notasResumen ?: "",
                    observacionesOperador = resumen?.observacionesOperador ?: ""
                ) 
            }
        }
    }

    fun guardarNotas(notas: String) {
        viewModelScope.launch {
            try {
                val existing = resumenInventarioDao.getResumenByActaId(actaUuid)
                if (existing != null) {
                    resumenInventarioDao.update(existing.copy(notasResumen = notas))
                } else {
                    resumenInventarioDao.insert(
                        ResumenInventarioEntity(uuidActa = actaUuid, notasResumen = notas)
                    )
                }
                _uiState.update { it.copy(notas = notas, guardadoExitoso = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error al guardar notas: ${e.message}") }
            }
        }
    }

    fun guardarObservacionesOperador(observacionesOperador: String) {
        viewModelScope.launch {
            try {
                val existing = resumenInventarioDao.getResumenByActaId(actaUuid)
                if (existing != null) {
                    resumenInventarioDao.update(existing.copy(observacionesOperador = observacionesOperador))
                } else {
                    resumenInventarioDao.insert(
                        ResumenInventarioEntity(uuidActa = actaUuid, observacionesOperador = observacionesOperador)
                    )
                }
                _uiState.update { it.copy(observacionesOperador = observacionesOperador, guardadoExitoso = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error al guardar observaciones del operador: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

}