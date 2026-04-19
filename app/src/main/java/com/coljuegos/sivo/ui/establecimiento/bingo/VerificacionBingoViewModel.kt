package com.coljuegos.sivo.ui.establecimiento.bingo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.dao.InventarioDao
import com.coljuegos.sivo.data.dao.VerificacionBingoDao
import com.coljuegos.sivo.data.entity.VerificacionBingoEntity
import com.coljuegos.sivo.data.entity.esBingo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class VerificacionBingoViewModel @Inject constructor(
    private val verificacionBingoDao: VerificacionBingoDao,
    private val inventarioDao: InventarioDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val actaUuid: UUID = checkNotNull(savedStateHandle.get<UUID>("actaUuid"))

    private val _uiState = MutableStateFlow(VerificacionBingoUiState())
    val uiState: StateFlow<VerificacionBingoUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, actaUuid = actaUuid) }
            try {
                // Verificar si el acta tiene inventarios bingo
                val inventarios = inventarioDao.getInventariosByActa(actaUuid)
                val tieneBingo = inventarios.any { it.esBingo() }

                // Cargar verificación existente
                val existente = verificacionBingoDao.getVerificacionBingoByActaId(actaUuid)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        tieneBingo = tieneBingo,
                        cartonesModulos = existente?.cartonesModulos ?: "",
                        sistemaTecnologico = existente?.sistemaTecnologico ?: "",
                        sistemaInterconectado = existente?.sistemaInterconectado ?: "",
                        realizaEventosEspeciales = existente?.realizaEventosEspeciales ?: "",
                        tipoBalotera = existente?.tipoBalotera ?: "",
                        valorCartonExpuesto = existente?.valorCartonExpuesto ?: ""
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun updateCartonesModulos(value: String) {
        _uiState.update { it.copy(cartonesModulos = value) }
        autoSave()
    }

    fun updateSistemaTecnologico(value: String) {
        _uiState.update { it.copy(sistemaTecnologico = value) }
        autoSave()
    }

    fun updateSistemaInterconectado(value: String) {
        _uiState.update { it.copy(sistemaInterconectado = value) }
        autoSave()
    }

    fun updateRealizaEventosEspeciales(value: String) {
        _uiState.update { it.copy(realizaEventosEspeciales = value) }
        autoSave()
    }

    fun updateTipoBalotera(value: String) {
        _uiState.update { it.copy(tipoBalotera = value) }
        autoSave()
    }

    fun updateValorCartonExpuesto(value: String) {
        _uiState.update { it.copy(valorCartonExpuesto = value) }
        autoSave()
    }

    private fun autoSave() {
        viewModelScope.launch {
            try {
                val state = _uiState.value
                val entity = VerificacionBingoEntity(
                    uuidActa = actaUuid,
                    cartonesModulos = state.cartonesModulos.takeIf { it.isNotBlank() },
                    sistemaTecnologico = state.sistemaTecnologico.takeIf { it.isNotBlank() },
                    sistemaInterconectado = state.sistemaInterconectado.takeIf { it.isNotBlank() },
                    realizaEventosEspeciales = state.realizaEventosEspeciales.takeIf { it.isNotBlank() },
                    tipoBalotera = state.tipoBalotera.takeIf { it.isNotBlank() },
                    valorCartonExpuesto = state.valorCartonExpuesto.takeIf { it.isNotBlank() }
                )
                // Preservar UUID si ya existe
                val existente = verificacionBingoDao.getVerificacionBingoByActaId(actaUuid)
                verificacionBingoDao.insert(
                    if (existente != null) entity.copy(uuidVerificacionBingo = existente.uuidVerificacionBingo)
                    else entity
                )
            } catch (_: Exception) { }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

}