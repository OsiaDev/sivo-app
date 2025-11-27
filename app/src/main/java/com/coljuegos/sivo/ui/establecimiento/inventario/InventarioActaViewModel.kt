package com.coljuegos.sivo.ui.establecimiento.inventario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.dao.InventarioDao
import com.coljuegos.sivo.data.dao.InventarioRegistradoDao
import com.coljuegos.sivo.data.entity.InventarioEntity
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

}