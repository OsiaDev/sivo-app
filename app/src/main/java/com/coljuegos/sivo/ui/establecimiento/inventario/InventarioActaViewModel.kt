package com.coljuegos.sivo.ui.establecimiento.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.dao.InventarioDao
import com.coljuegos.sivo.data.dao.InventarioRegistradoDao
import com.coljuegos.sivo.data.entity.EstadoInventarioEnum
import com.coljuegos.sivo.data.entity.InventarioEntity
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
class InventarioActaViewModel @Inject constructor(
    private val inventarioDao: InventarioDao,
    private val inventarioRegistradoDao: InventarioRegistradoDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventarioActaUiState())
    val uiState: StateFlow<InventarioActaUiState> = _uiState.asStateFlow()

    fun loadInventariosNoRegistrados(actaUuid: UUID) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                // Obtener inventarios registrados con Flow
                inventarioRegistradoDao.getInventariosRegistradosByActa(actaUuid).collect { registrados ->
                    // Obtener todos los inventarios del acta
                    val todosInventarios = inventarioDao.getInventariosByActa(actaUuid)

                    // Filtrar solo los que NO están registrados
                    val uuidsRegistrados = registrados.map { it.uuidInventario }.toSet()
                    val inventariosNoRegistrados = todosInventarios.filter { inventario ->
                        !uuidsRegistrados.contains(inventario.uuidInventario)
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            inventariosNoRegistrados = inventariosNoRegistrados,
                            filteredInventarios = inventariosNoRegistrados,
                            totalInventariosNoRegistrados = inventariosNoRegistrados.size,
                            errorMessage = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar inventarios: ${e.message}"
                    )
                }
            }
        }
    }

    fun filterInventario(query: String) {
        val currentState = _uiState.value

        if (query.isEmpty()) {
            _uiState.update {
                it.copy(
                    filteredInventarios = currentState.inventariosNoRegistrados,
                    searchQuery = query
                )
            }
        } else {
            val filtered = currentState.inventariosNoRegistrados.filter { inventario ->
                inventario.nombreMarcaInventario.contains(query, ignoreCase = true) ||
                        inventario.metSerialInventario.contains(query, ignoreCase = true) ||
                        inventario.nucInventario.contains(query, ignoreCase = true)
            }

            _uiState.update {
                it.copy(
                    filteredInventarios = filtered,
                    searchQuery = query
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun registrarTodosComoNoEncontrados(actaUuid: UUID) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                val inventariosNoRegistrados = _uiState.value.inventariosNoRegistrados
                if (inventariosNoRegistrados.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@launch
                }

                inventariosNoRegistrados.forEach { inventario ->
                    val registro = InventarioRegistradoEntity(
                        uuidInventarioRegistrado = UUID.randomUUID(),
                        uuidActa = actaUuid,
                        uuidInventario = inventario.uuidInventario,
                        codigoApuestaDiferente = false,
                        codigoApuestaDiferenteValor = null,
                        serialVerificado = false,
                        serialDiferente = null,
                        descripcionJuego = false,
                        planPremios = false,
                        valorPremios = false,
                        valorCredito = null,
                        contadoresVerificado = false,
                        coinInMet = null,
                        coinOutMet = null,
                        jackpotMet = null,
                        coinInSclm = null,
                        coinOutSclm = null,
                        jackpotSclm = null,
                        observaciones = "No encontrado",
                        numeroInternoMetOperador = null,
                        estado = EstadoInventarioEnum.NO_ENCONTRADO
                    )
                    inventarioRegistradoDao.insert(registro)
                }

                _uiState.update { it.copy(isLoading = false, registroMasivoExitoso = true) }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al registrar inventarios: ${e.message}"
                    )
                }
            }
        }
    }

}