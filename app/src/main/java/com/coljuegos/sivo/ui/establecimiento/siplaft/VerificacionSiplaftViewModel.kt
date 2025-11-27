package com.coljuegos.sivo.ui.establecimiento.siplaft

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.dao.VerificacionSiplaftDao
import com.coljuegos.sivo.data.entity.VerificacionSiplaftEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class VerificacionSiplaftViewModel @Inject constructor(
    private val verificacionSiplaftDao: VerificacionSiplaftDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val actaUuid: UUID = checkNotNull(savedStateHandle.get<UUID>("actaUuid"))

    private val _uiState = MutableStateFlow(VerificacionSiplaftUiState())
    val uiState: StateFlow<VerificacionSiplaftUiState> = _uiState.asStateFlow()

    init {
        loadVerificacionSiplaft()
    }

    private fun loadVerificacionSiplaft() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                val verificacion = verificacionSiplaftDao.getVerificacionSiplaftByActaId(actaUuid)

                if (verificacion != null) {
                    val cuentaIdentificacion = verificacion.cuentaFormatoIdentificacion == "Si"
                    val cuentaReporte = verificacion.cuentaFormatoReporteInterno == "Si"

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actaUuid = actaUuid,
                        cuentaFormatoIdentificacion = verificacion.cuentaFormatoIdentificacion ?: "",
                        montoIdentificacion = verificacion.montoIdentificacion ?: "",
                        cuentaFormatoReporteInterno = verificacion.cuentaFormatoReporteInterno ?: "",
                        senalesAlerta = verificacion.senalesAlerta ?: "",
                        conoceCodigoConducta = verificacion.conoceCodigoConducta ?: "",
                        mostrarCampoMonto = cuentaIdentificacion,
                        mostrarCampoSenales = cuentaReporte
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, actaUuid = actaUuid)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar verificación SIPLAFT: ${e.message}"
                )
            }
        }
    }

    fun updateCuentaFormatoIdentificacion(value: String) {
        val mostrarMonto = value == "Si"
        _uiState.value = _uiState.value.copy(
            cuentaFormatoIdentificacion = value,
            mostrarCampoMonto = mostrarMonto
        )

        // Si se oculta el campo, limpiar el valor
        if (!mostrarMonto) {
            _uiState.value = _uiState.value.copy(montoIdentificacion = "")
        }

        saveVerificacionSiplaft()
    }

    fun updateMontoIdentificacion(value: String) {
        _uiState.value = _uiState.value.copy(montoIdentificacion = value)
        saveVerificacionSiplaft()
    }

    fun updateCuentaFormatoReporteInterno(value: String) {
        val mostrarSenales = value == "Si"
        _uiState.value = _uiState.value.copy(
            cuentaFormatoReporteInterno = value,
            mostrarCampoSenales = mostrarSenales
        )

        // Si se oculta el campo, limpiar el valor
        if (!mostrarSenales) {
            _uiState.value = _uiState.value.copy(senalesAlerta = "")
        }

        saveVerificacionSiplaft()
    }

    fun updateSenalesAlerta(value: String) {
        _uiState.value = _uiState.value.copy(senalesAlerta = value)
        saveVerificacionSiplaft()
    }

    fun updateConoceCodigoConducta(value: String) {
        _uiState.value = _uiState.value.copy(conoceCodigoConducta = value)
        saveVerificacionSiplaft()
    }

    private fun saveVerificacionSiplaft() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value

                // Buscar si ya existe un registro
                val existingVerificacion = verificacionSiplaftDao.getVerificacionSiplaftByActaId(actaUuid)

                val verificacionToSave = existingVerificacion?.copy(
                    cuentaFormatoIdentificacion = currentState.cuentaFormatoIdentificacion.takeIf { it.isNotBlank() },
                    montoIdentificacion = currentState.montoIdentificacion.takeIf { it.isNotBlank() },
                    cuentaFormatoReporteInterno = currentState.cuentaFormatoReporteInterno.takeIf { it.isNotBlank() },
                    senalesAlerta = currentState.senalesAlerta.takeIf { it.isNotBlank() },
                    conoceCodigoConducta = currentState.conoceCodigoConducta.takeIf { it.isNotBlank() }
                ) ?: VerificacionSiplaftEntity(
                    uuidActa = actaUuid,
                    cuentaFormatoIdentificacion = currentState.cuentaFormatoIdentificacion.takeIf { it.isNotBlank() },
                    montoIdentificacion = currentState.montoIdentificacion.takeIf { it.isNotBlank() },
                    cuentaFormatoReporteInterno = currentState.cuentaFormatoReporteInterno.takeIf { it.isNotBlank() },
                    senalesAlerta = currentState.senalesAlerta.takeIf { it.isNotBlank() },
                    conoceCodigoConducta = currentState.conoceCodigoConducta.takeIf { it.isNotBlank() }
                )

                verificacionSiplaftDao.insert(verificacionToSave)
            } catch (e: Exception) {
                // Error silencioso para no interrumpir la experiencia del usuario
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun retry() {
        loadVerificacionSiplaft()
    }

}