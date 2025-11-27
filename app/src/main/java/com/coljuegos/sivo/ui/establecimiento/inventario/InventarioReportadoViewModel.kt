package com.coljuegos.sivo.ui.establecimiento.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.dao.InventarioDao
import com.coljuegos.sivo.data.dao.InventarioRegistradoDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class InventarioReportadoViewModel @Inject constructor(
    private val inventarioDao: InventarioDao,
    private val inventarioRegistradoDao: InventarioRegistradoDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventarioReportadoUiState())
    val uiState: StateFlow<InventarioReportadoUiState> = _uiState.asStateFlow()

    fun loadInventariosRegistrados(actaUuid: UUID) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                // Obtener inventarios registrados con Flow
                inventarioRegistradoDao.getInventariosRegistradosByActa(actaUuid).collect { registrados ->
                    // Obtener todos los inventarios del acta
                    val todosInventarios = inventarioDao.getInventariosByActa(actaUuid)

                    // Crear lista de InventarioConRegistro
                    val inventariosConRegistro = todosInventarios.map { inventario ->
                        val registro = registrados.find { it.uuidInventario == inventario.uuidInventario }
                        InventarioConRegistro(
                            inventario = inventario,
                            registro = registro
                        )
                    }

                    // Filtrar solo los que están registrados
                    val soloRegistrados = inventariosConRegistro.filter { it.registro != null }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            inventariosRegistrados = soloRegistrados,
                            totalInventariosRegistrados = soloRegistrados.size,
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

    fun eliminarInventarioRegistrado(uuidInventarioRegistrado: UUID) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                inventarioRegistradoDao.deleteById(uuidInventarioRegistrado)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al eliminar inventario: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

}