package com.coljuegos.sivo.ui.establecimiento.bingo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.dao.InventarioBingoRegistradoDao
import com.coljuegos.sivo.data.dao.InventarioDao
import com.coljuegos.sivo.data.entity.InventarioEntity
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
class InventarioBingoActaViewModel @Inject constructor(
    private val inventarioDao: InventarioDao,
    private val inventarioBingoRegistradoDao: InventarioBingoRegistradoDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventarioBingoActaUiState())
    val uiState: StateFlow<InventarioBingoActaUiState> = _uiState.asStateFlow()

    fun loadNoRegistrados(actaUuid: UUID) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                inventarioBingoRegistradoDao.getByActa(actaUuid).collect { registrados ->
                    val todosLosBingos = inventarioDao.getInventariosByActa(actaUuid)
                        .filter { it.esBingo() }
                    val uuidsRegistrados = registrados.map { it.uuidInventario }.toSet()
                    val sinRegistrar = todosLosBingos.filter {
                        it.uuidInventario !in uuidsRegistrados
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            inventarios = sinRegistrar,
                            filteredInventarios = sinRegistrar.applyFilter(it.searchQuery)
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
                filteredInventarios = it.inventarios.applyFilter(q)
            )
        }
    }

    private fun List<InventarioEntity>.applyFilter(query: String) =
        if (query.isBlank()) this
        else filter {
            it.tipoApuestaNombreInventario.lowercase().contains(query.lowercase()) ||
                    it.insCodigoInventario.lowercase().contains(query.lowercase())
        }

}