package com.coljuegos.sivo.ui.establecimiento.bingo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.dao.InventarioBingoRegistradoDao
import com.coljuegos.sivo.data.dao.InventarioDao
import com.coljuegos.sivo.data.entity.esBingo
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

    fun loadInventarios(actaUuid: UUID) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val inventariosBingo = inventarioDao.getInventariosByActa(actaUuid)
                    .filter { it.esBingo() }
                val registros = inventarioBingoRegistradoDao.getByActaList(actaUuid)

                val combinados = inventariosBingo.map { inv ->
                    InventarioBingoConRegistro(
                        inventario = inv,
                        registro = registros.firstOrNull { it.uuidInventario == inv.uuidInventario }
                    )
                }

                val registrados = combinados.count { it.registro != null }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        inventarios = combinados,
                        filteredInventarios = combinados.applyFilter(it.searchQuery),
                        totalRegistrados = registrados
                    )
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
                filteredInventarios = it.inventarios.applyFilter(q)
            )
        }
    }

    fun deleteRegistro(uuid: UUID, actaUuid: UUID) {
        viewModelScope.launch {
            inventarioBingoRegistradoDao.deleteById(uuid)
            loadInventarios(actaUuid)
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