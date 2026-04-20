package com.coljuegos.sivo.ui.establecimiento.bingo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.dao.InventarioBingoRegistradoDao
import com.coljuegos.sivo.data.dao.InventarioDao
import com.coljuegos.sivo.data.entity.EstadoInventarioEnum
import com.coljuegos.sivo.data.entity.InventarioBingoRegistradoEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RegistrarBingoViewModel @Inject constructor(
    private val inventarioDao: InventarioDao,
    private val inventarioBingoRegistradoDao: InventarioBingoRegistradoDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val actaUuid: UUID = checkNotNull(savedStateHandle.get<UUID>("actaUuid"))
    private val inventarioUuid: UUID = checkNotNull(savedStateHandle.get<UUID>("inventarioUuid"))
    private val registroUuid: UUID? = savedStateHandle.get<UUID>("inventarioBingoRegistradoUuid")

    private val _uiState = MutableStateFlow(RegistrarBingoUiState())
    val uiState: StateFlow<RegistrarBingoUiState> = _uiState.asStateFlow()

    fun loadDatos(actaUuid: UUID, inventarioUuid: UUID, registroUuid: UUID?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val inventario = inventarioDao.getInventarioByUuid(inventarioUuid)
                val registro = registroUuid?.let {
                    inventarioBingoRegistradoDao.getById(it)
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        inventario = inventario,
                        registro = registro,
                        esEdicion = registro != null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun guardar(
        codigoApuestaDiferente: Boolean,
        codigoApuestaDiferenteValor: String?,
        sillasDiferente: Boolean,
        sillasValor: Int?,
        estado: EstadoInventarioEnum,
        observaciones: String?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val existente = _uiState.value.registro
                val entity = InventarioBingoRegistradoEntity(
                    uuidInventarioBingoRegistrado = existente?.uuidInventarioBingoRegistrado ?: UUID.randomUUID(),
                    uuidActa = actaUuid,
                    uuidInventario = inventarioUuid,
                    codigoApuestaDiferente = codigoApuestaDiferente,
                    codigoApuestaDiferenteValor = if (codigoApuestaDiferente) codigoApuestaDiferenteValor else null,
                    sillasDiferente = sillasDiferente,
                    sillasValor = if (sillasDiferente) sillasValor else null,
                    estado = estado,
                    observaciones = observaciones?.takeIf { it.isNotBlank() }
                )
                inventarioBingoRegistradoDao.insert(entity)
                _uiState.update { it.copy(isLoading = false, guardadoExitoso = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

}