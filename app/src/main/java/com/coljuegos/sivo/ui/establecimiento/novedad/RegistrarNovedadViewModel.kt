package com.coljuegos.sivo.ui.establecimiento.novedad

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.dao.InventarioDao
import com.coljuegos.sivo.data.dao.NovedadRegistradaDao
import com.coljuegos.sivo.data.entity.NovedadRegistradaEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RegistrarNovedadViewModel @Inject constructor(
    private val inventarioDao: InventarioDao,
    private val novedadRegistradaDao: NovedadRegistradaDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val actaUuid: UUID = checkNotNull(savedStateHandle.get<UUID>("actaUuid"))

    private val novedadRegistradaUuid: UUID? = savedStateHandle.get<UUID>("novedadRegistradaUuid")

    private val _uiState = MutableStateFlow(RegistrarNovedadUiState())

    val uiState: StateFlow<RegistrarNovedadUiState> = _uiState.asStateFlow()

    init {
        loadInventario()
    }

    private fun loadInventario() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                // Obtener el inventario
                val todosInventarios = inventarioDao.getInventariosByActa(actaUuid)

                // Si es edición, obtener el registro de novedad
                val novedad = novedadRegistradaUuid?.let { uuid ->
                    novedadRegistradaDao.getNovedadRegistradaById(uuid)
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        novedadRegistrada = novedad,
                        esEdicion = novedad != null,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar inventario: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Valida si ya existe una novedad registrada con el mismo serial
     * Retorna true si el serial está duplicado
     */
    private suspend fun validarSerialDuplicado(serial: String): Boolean {
        // Si estamos en modo edición, permitir guardar con el mismo serial
        if (novedadRegistradaUuid != null) {
            return false
        }

        // Obtener todas las novedades del acta actual
        val novedadesExistentes = novedadRegistradaDao.getNovedadesRegistradasByActaList(actaUuid)

        // Verificar si alguna novedad tiene el mismo serial
        return novedadesExistentes.any { it.serial.equals(serial, ignoreCase = true) }
    }

    fun guardarNovedad(
        serial: String,
        marca: String,
        codigoApuesta: String,
        operando: String,
        tienePlaca: Boolean,
        valorCredito: String?,
        coinInMet: String?,
        coinOutMet: String?,
        jackpotMet: String?,
        coinInSclm: String?,
        coinOutSclm: String?,
        jackpotSclm: String?,
        observaciones: String?
    ) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                // Validar campos obligatorios
                if (tienePlaca && serial.isBlank()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            guardadoExitoso = false,
                            errorMessage = "El serial es obligatorio"
                        )
                    }
                    return@launch
                }

                if (marca.isBlank()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            guardadoExitoso = false,
                            errorMessage = "La marca es obligatoria"
                        )
                    }
                    return@launch
                }

                if (operando.isBlank()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            guardadoExitoso = false,
                            errorMessage = "Debe seleccionar el estado operativo"
                        )
                    }
                    return@launch
                }

                // Validar serial duplicado (solo en modo creación)
                if (tienePlaca && validarSerialDuplicado(serial)) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            guardadoExitoso = false,
                            errorMessage = "Ya existe una novedad registrada con el serial: $serial. No se pueden registrar dos novedades con el mismo serial."
                        )
                    }
                    return@launch
                }

                // Crear o actualizar la novedad
                val novedadRegistrada = NovedadRegistradaEntity(
                    uuidNovedadRegistrada = novedadRegistradaUuid ?: UUID.randomUUID(),
                    uuidActa = actaUuid,
                    serial = if (tienePlaca) serial.trim() else "",
                    marca = marca.trim(),
                    codigoApuesta = codigoApuesta.trim(),
                    tienePlaca = tienePlaca,
                    operando = operando.trim(),
                    valorCredito = valorCredito?.trim()?.takeIf { it.isNotBlank() },
                    coinInMet = coinInMet?.trim()?.takeIf { it.isNotBlank() },
                    coinOutMet = coinOutMet?.trim()?.takeIf { it.isNotBlank() },
                    jackpotMet = jackpotMet?.trim()?.takeIf { it.isNotBlank() },
                    coinInSclm = coinInSclm?.trim()?.takeIf { it.isNotBlank() },
                    coinOutSclm = coinOutSclm?.trim()?.takeIf { it.isNotBlank() },
                    jackpotSclm = jackpotSclm?.trim()?.takeIf { it.isNotBlank() },
                    observaciones = observaciones?.trim()?.takeIf { it.isNotBlank() }
                )

                // Insertar o actualizar
                novedadRegistradaDao.insert(novedadRegistrada)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        guardadoExitoso = true,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        guardadoExitoso = false,
                        errorMessage = "Error al guardar novedad: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun resetGuardadoExitoso() {
        _uiState.update { it.copy(guardadoExitoso = false) }
    }

}