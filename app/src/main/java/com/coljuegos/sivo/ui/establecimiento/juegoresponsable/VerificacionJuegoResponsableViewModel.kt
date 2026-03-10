package com.coljuegos.sivo.ui.establecimiento.juegoresponsable

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.dao.VerificacionJuegoResponsableDao
import com.coljuegos.sivo.data.entity.VerificacionJuegoResponsableEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class VerificacionJuegoResponsableViewModel @Inject constructor(
    private val verificacionJuegoResponsableDao: VerificacionJuegoResponsableDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val actaUuid: UUID = checkNotNull(savedStateHandle.get<UUID>("actaUuid"))

    private val _uiState = MutableStateFlow(VerificacionJuegoResponsableUiState())
    val uiState: StateFlow<VerificacionJuegoResponsableUiState> = _uiState.asStateFlow()

    init {
        loadVerificacion()
    }

    private fun loadVerificacion() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                val verificacion = verificacionJuegoResponsableDao.getVerificacionByActaId(actaUuid)
                if (verificacion != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actaUuid = actaUuid,
                        cuentaTestIdentificacionRiesgos = verificacion.cuentaTestIdentificacionRiesgos ?: "",
                        existenPiezasPublicitarias = verificacion.existenPiezasPublicitarias ?: "",
                        cuentaProgramaJuegoResponsable = verificacion.cuentaProgramaJuegoResponsable ?: ""
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, actaUuid = actaUuid)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar verificación: ${e.message}"
                )
            }
        }
    }

    fun updateCuentaTestIdentificacionRiesgos(value: String) {
        _uiState.value = _uiState.value.copy(cuentaTestIdentificacionRiesgos = value)
        saveVerificacion()
    }

    fun updateExistenPiezasPublicitarias(value: String) {
        _uiState.value = _uiState.value.copy(existenPiezasPublicitarias = value)
        saveVerificacion()
    }

    fun updateCuentaProgramaJuegoResponsable(value: String) {
        _uiState.value = _uiState.value.copy(cuentaProgramaJuegoResponsable = value)
        saveVerificacion()
    }

    private fun saveVerificacion() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                val existing = verificacionJuegoResponsableDao.getVerificacionByActaId(actaUuid)
                val toSave = existing?.copy(
                    cuentaTestIdentificacionRiesgos = currentState.cuentaTestIdentificacionRiesgos.takeIf { it.isNotBlank() },
                    existenPiezasPublicitarias = currentState.existenPiezasPublicitarias.takeIf { it.isNotBlank() },
                    cuentaProgramaJuegoResponsable = currentState.cuentaProgramaJuegoResponsable.takeIf { it.isNotBlank() }
                ) ?: VerificacionJuegoResponsableEntity(
                    uuidActa = actaUuid,
                    cuentaTestIdentificacionRiesgos = currentState.cuentaTestIdentificacionRiesgos.takeIf { it.isNotBlank() },
                    existenPiezasPublicitarias = currentState.existenPiezasPublicitarias.takeIf { it.isNotBlank() },
                    cuentaProgramaJuegoResponsable = currentState.cuentaProgramaJuegoResponsable.takeIf { it.isNotBlank() }
                )
                verificacionJuegoResponsableDao.insert(toSave)
            } catch (_: Exception) { }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

}