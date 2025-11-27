package com.coljuegos.sivo.ui.establecimiento.inventario

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.dao.InventarioDao
import com.coljuegos.sivo.data.dao.InventarioRegistradoDao
import com.coljuegos.sivo.data.entity.EstadoInventarioEnum
import com.coljuegos.sivo.data.entity.InventarioRegistradoEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RegistrarInventarioViewModel @Inject constructor(
    private val inventarioDao: InventarioDao,
    private val inventarioRegistradoDao: InventarioRegistradoDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val actaUuid: UUID = checkNotNull(savedStateHandle.get<UUID>("actaUuid"))
    private val inventarioUuid: UUID = checkNotNull(savedStateHandle.get<UUID>("inventarioUuid"))
    private val inventarioRegistradoUuid: UUID? = savedStateHandle.get<UUID>("inventarioRegistradoUuid")

    private val _uiState = MutableStateFlow(RegistrarInventarioUiState())
    val uiState: StateFlow<RegistrarInventarioUiState> = _uiState.asStateFlow()

    fun loadInventario(actaUuid: UUID, inventarioUuid: UUID, inventarioRegistradoUuid: UUID?) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                // Obtener el inventario
                val todosInventarios = inventarioDao.getInventariosByActa(actaUuid)
                val inventario = todosInventarios.find { it.uuidInventario == inventarioUuid }

                // Si es edición, obtener el registro
                val registro = inventarioRegistradoUuid?.let { uuid ->
                    inventarioRegistradoDao.getInventarioRegistradoById(uuid)
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        inventario = inventario,
                        inventarioRegistrado = registro,
                        esEdicion = registro != null,
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

    fun guardarInventario(
        codigoApuestaDiferente: Boolean,
        codigoApuestaDiferenteValor: String?,
        serialVerificado: Boolean,
        serialDiferente: String?,
        descripcionJuego: Boolean,
        planPremios: Boolean,
        valorPremios: Boolean,
        valorCredito: String?,
        contadoresVerificado: Boolean,
        coinInMet: String?,
        coinOutMet: String?,
        jackpotMet: String?,
        coinInSclm: String?,
        coinOutSclm: String?,
        jackpotSclm: String?,
        observaciones: String?,
        estado: String
    ) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                val inventarioRegistrado = InventarioRegistradoEntity(
                    uuidInventarioRegistrado = inventarioRegistradoUuid ?: UUID.randomUUID(),
                    uuidActa = actaUuid,
                    uuidInventario = inventarioUuid,
                    codigoApuestaDiferente = codigoApuestaDiferente,
                    codigoApuestaDiferenteValor = codigoApuestaDiferenteValor,
                    serialVerificado = serialVerificado,
                    serialDiferente = serialDiferente,
                    descripcionJuego = descripcionJuego,
                    planPremios = planPremios,
                    valorPremios = valorPremios,
                    valorCredito = valorCredito,
                    contadoresVerificado = contadoresVerificado,
                    coinInMet = coinInMet,
                    coinOutMet = coinOutMet,
                    jackpotMet = jackpotMet,
                    coinInSclm = coinInSclm,
                    coinOutSclm = coinOutSclm,
                    jackpotSclm = jackpotSclm,
                    observaciones = observaciones,
                    estado = EstadoInventarioEnum.fromString(estado)
                )

                // Insertar o actualizar
                inventarioRegistradoDao.insert(inventarioRegistrado)

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
                        errorMessage = "Error al guardar inventario: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
}