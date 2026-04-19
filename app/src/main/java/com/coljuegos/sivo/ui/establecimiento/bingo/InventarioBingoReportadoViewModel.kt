package com.coljuegos.sivo.ui.establecimiento.bingo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.dao.InventarioBingoRegistradoDao
import com.coljuegos.sivo.data.dao.InventarioDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class InventarioBingoReportadoViewModel @Inject constructor(
    private val inventarioDao: InventarioDao,
    private val inventarioBingoRegistradoDao: InventarioBingoRegistradoDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventarioBingoReportadoUiState())
    val uiState: StateFlow<InventarioBingoReportadoUiState> = _uiState.asStateFlow()

    fun loadRegistrados(actaUuid: UUID) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                inventarioBingoRegistradoDao.getByActa(actaUuid).collect { registros ->
                    val combinados = registros.mapNotNull { reg ->
                        val inv = inventarioDao.getInventarioByUuid(reg.uuidInventario)
                            ?: return@mapNotNull null
                        InventarioBingoConRegistro(inventario = inv, registro = reg)
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            registrados = combinados,
                            filteredRegistrados = combinados.applyFilter(it.searchQuery)
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun filterInventario(query: String?) {
        val q = query ?: ""
        _uiState.update {
            it.copy(
                searchQuery = q,
                filteredRegistrados = it.registrados.applyFilter(q)
            )
        }
    }

    fun deleteRegistro(uuid: UUID) {
        viewModelScope.launch {
            inventarioBingoRegistradoDao.deleteById(uuid)
        }
    }

    private fun List<InventarioBingoConRegistro>.applyFilter(query: String): List<InventarioBingoConRegistro> {
        if (query.isBlank()) return this
        val q = query.lowercase()
        return filter {
            it.inventario.tipoApuestaNombreInventario.lowercase().contains(q) ||
                    it.inventario.insCodigoInventario.lowercase().contains(q)
        }
    }

}