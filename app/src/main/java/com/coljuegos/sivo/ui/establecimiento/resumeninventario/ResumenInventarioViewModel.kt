package com.coljuegos.sivo.ui.establecimiento.resumeninventario

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.dao.InventarioRegistradoDao
import com.coljuegos.sivo.data.dao.NovedadRegistradaDao
import com.coljuegos.sivo.data.entity.EstadoInventarioEnum
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
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val actaUuid: UUID = checkNotNull(savedStateHandle.get<UUID>("actaUuid"))

    private val _uiState = MutableStateFlow(ResumenInventarioUiState())
    val uiState: StateFlow<ResumenInventarioUiState> = _uiState.asStateFlow()

    // Firma
    private val _signatureBitmap = MutableStateFlow<Bitmap?>(null)
    val signatureBitmap: StateFlow<Bitmap?> = _signatureBitmap.asStateFlow()

    init {
        calcularEstadisticas()
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

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

}