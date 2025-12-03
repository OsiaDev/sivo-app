package com.coljuegos.sivo.ui.establecimiento.verificacion

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.dao.VerificacionContractualDao
import com.coljuegos.sivo.data.entity.VerificacionContractualEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class VerificacionContractualViewModel @Inject constructor(
    private val verificacionContractualDao: VerificacionContractualDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val actaUuid: UUID = checkNotNull(savedStateHandle.get<UUID>("actaUuid"))

    private val _uiState = MutableStateFlow(VerificacionContractualUiState())
    val uiState: StateFlow<VerificacionContractualUiState> = _uiState.asStateFlow()

    init {
        loadVerificacionContractual()
    }

    private fun loadVerificacionContractual() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null,
                    actaUuid = actaUuid  // AGREGAR ESTA LÍNEA
                )

                val verificacion = verificacionContractualDao.getVerificacionContractualByActaId(actaUuid)

                if (verificacion != null) {
                    val desarrollaActividades = verificacion.desarrollaActividadesDiferentes == "Si"
                    val esTipoOtros = verificacion.tipoActividad == "Otros"
                    val mostrarOtraDireccion = verificacion.direccionCorresponde == "No"
                    val mostrarOtroNombre = verificacion.nombreEstablecimientoCorresponde == "No"

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        actaUuid = actaUuid,
                        avisoAutorizacion = verificacion.avisoAutorizacion ?: "",
                        direccionCorresponde = verificacion.direccionCorresponde ?: "",
                        otraDireccion = verificacion.otraDireccion ?: "",
                        nombreEstablecimientoCorresponde = verificacion.nombreEstablecimientoCorresponde ?: "",
                        otroNombre = verificacion.otroNombre ?: "",
                        desarrollaActividadesDiferentes = verificacion.desarrollaActividadesDiferentes ?: "",
                        tipoActividad = verificacion.tipoActividad ?: "",
                        especificacionOtros = verificacion.especificacionOtros ?: "",
                        cuentaRegistrosMantenimiento = verificacion.cuentaRegistrosMantenimiento ?: "",
                        mostrarCampoOtraDireccion = mostrarOtraDireccion,
                        mostrarCampoOtroNombre = mostrarOtroNombre,
                        mostrarSeccionActividadesDiferentes = desarrollaActividades,
                        mostrarCampoOtros = esTipoOtros
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar verificación contractual: ${e.message}"
                )
            }
        }
    }

    fun updateAvisoAutorizacion(value: String) {
        _uiState.value = _uiState.value.copy(avisoAutorizacion = value)
        saveVerificacionContractual()
    }

    fun updateDireccionCorresponde(value: String) {
        val mostrarOtraDireccion = value == "No"
        _uiState.value = _uiState.value.copy(
            direccionCorresponde = value,
            mostrarCampoOtraDireccion = mostrarOtraDireccion
        )

        // Si no es "No", limpiar el campo
        if (!mostrarOtraDireccion) {
            _uiState.value = _uiState.value.copy(otraDireccion = "")
        }

        saveVerificacionContractual()
    }

    fun updateOtraDireccion(value: String) {
        _uiState.value = _uiState.value.copy(otraDireccion = value)
        saveVerificacionContractual()
    }

    fun updateNombreEstablecimientoCorresponde(value: String) {
        val mostrarOtroNombre = value == "No"
        _uiState.value = _uiState.value.copy(
            nombreEstablecimientoCorresponde = value,
            mostrarCampoOtroNombre = mostrarOtroNombre
        )

        // Si no es "No", limpiar el campo
        if (!mostrarOtroNombre) {
            _uiState.value = _uiState.value.copy(otroNombre = "")
        }

        saveVerificacionContractual()
    }

    fun updateOtroNombre(value: String) {
        _uiState.value = _uiState.value.copy(otroNombre = value)
        saveVerificacionContractual()
    }

    fun updateDesarrollaActividadesDiferentes(value: String) {
        val mostrarSeccion = value == "Si"
        _uiState.value = _uiState.value.copy(
            desarrollaActividadesDiferentes = value,
            mostrarSeccionActividadesDiferentes = mostrarSeccion
        )

        // Si se oculta la sección, limpiar los valores relacionados
        if (!mostrarSeccion) {
            _uiState.value = _uiState.value.copy(
                tipoActividad = "",
                especificacionOtros = "",
                mostrarCampoOtros = false
            )
        }

        saveVerificacionContractual()
    }

    fun updateTipoActividad(value: String) {
        val mostrarOtros = value == "Otros"
        _uiState.value = _uiState.value.copy(
            tipoActividad = value,
            mostrarCampoOtros = mostrarOtros
        )

        // Si no es "Otros", limpiar la especificación
        if (!mostrarOtros) {
            _uiState.value = _uiState.value.copy(especificacionOtros = "")
        }

        saveVerificacionContractual()
    }

    fun updateEspecificacionOtros(value: String) {
        _uiState.value = _uiState.value.copy(especificacionOtros = value)
        saveVerificacionContractual()
    }

    fun updateCuentaRegistrosMantenimiento(value: String) {
        _uiState.value = _uiState.value.copy(cuentaRegistrosMantenimiento = value)
        saveVerificacionContractual()
    }

    private fun saveVerificacionContractual() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value

                // Buscar si ya existe un registro
                val existingVerificacion = verificacionContractualDao.getVerificacionContractualByActaId(actaUuid)

                val verificacionToSave = existingVerificacion?.copy(
                    avisoAutorizacion = currentState.avisoAutorizacion.takeIf { it.isNotBlank() },
                    direccionCorresponde = currentState.direccionCorresponde.takeIf { it.isNotBlank() },
                    otraDireccion = currentState.otraDireccion.takeIf { it.isNotBlank() },
                    nombreEstablecimientoCorresponde = currentState.nombreEstablecimientoCorresponde.takeIf { it.isNotBlank() },
                    otroNombre = currentState.otroNombre.takeIf { it.isNotBlank() },
                    desarrollaActividadesDiferentes = currentState.desarrollaActividadesDiferentes.takeIf { it.isNotBlank() },
                    tipoActividad = currentState.tipoActividad.takeIf { it.isNotBlank() },
                    especificacionOtros = currentState.especificacionOtros.takeIf { it.isNotBlank() },
                    cuentaRegistrosMantenimiento = currentState.cuentaRegistrosMantenimiento.takeIf { it.isNotBlank() }
                ) ?: VerificacionContractualEntity(
                    uuidActa = actaUuid,
                    avisoAutorizacion = currentState.avisoAutorizacion.takeIf { it.isNotBlank() },
                    direccionCorresponde = currentState.direccionCorresponde.takeIf { it.isNotBlank() },
                    otraDireccion = currentState.otraDireccion.takeIf { it.isNotBlank() },
                    nombreEstablecimientoCorresponde = currentState.nombreEstablecimientoCorresponde.takeIf { it.isNotBlank() },
                    otroNombre = currentState.otroNombre.takeIf { it.isNotBlank() },
                    desarrollaActividadesDiferentes = currentState.desarrollaActividadesDiferentes.takeIf { it.isNotBlank() },
                    tipoActividad = currentState.tipoActividad.takeIf { it.isNotBlank() },
                    especificacionOtros = currentState.especificacionOtros.takeIf { it.isNotBlank() },
                    cuentaRegistrosMantenimiento = currentState.cuentaRegistrosMantenimiento.takeIf { it.isNotBlank() }
                )

                verificacionContractualDao.insert(verificacionToSave)
            } catch (e: Exception) {
                // Error silencioso para no interrumpir la experiencia del usuario
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun retry() {
        loadVerificacionContractual()
    }

    fun validateForm(): Boolean {
        val currentState = _uiState.value

        // Validar campo obligatorio: Aviso de autorización
        if (currentState.avisoAutorizacion.isBlank()) {
            return false
        }

        // Validar campo obligatorio: Dirección corresponde
        if (currentState.direccionCorresponde.isBlank()) {
            return false
        }

        // Si la dirección NO corresponde, validar que se haya ingresado la dirección correcta
        if (currentState.mostrarCampoOtraDireccion && currentState.otraDireccion.isBlank()) {
            return false
        }

        // Validar campo obligatorio: Nombre establecimiento corresponde
        if (currentState.nombreEstablecimientoCorresponde.isBlank()) {
            return false
        }

        // Si el nombre NO corresponde, validar que se haya ingresado el nombre correcto
        if (currentState.mostrarCampoOtroNombre && currentState.otroNombre.isBlank()) {
            return false
        }

        // Validar campo obligatorio: Desarrolla actividades diferentes
        if (currentState.desarrollaActividadesDiferentes.isBlank()) {
            return false
        }

        // Si desarrolla actividades diferentes, validar campos relacionados
        if (currentState.mostrarSeccionActividadesDiferentes) {
            // Debe seleccionar el tipo de actividad
            if (currentState.tipoActividad.isBlank()) {
                return false
            }

            // Si es "Otros", debe especificar
            if (currentState.mostrarCampoOtros && currentState.especificacionOtros.isBlank()) {
                return false
            }
        }

        // Validar campo obligatorio: Cuenta con registros de mantenimiento
        if (currentState.cuentaRegistrosMantenimiento.isBlank()) {
            return false
        }

        return true
    }

}