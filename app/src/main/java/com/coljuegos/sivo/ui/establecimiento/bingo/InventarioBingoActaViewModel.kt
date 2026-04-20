package com.coljuegos.sivo.ui.establecimiento.bingo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.dao.InventarioBingoRegistradoDao
import com.coljuegos.sivo.data.dao.InventarioDao
import com.coljuegos.sivo.data.entity.EstadoInventarioEnum
import com.coljuegos.sivo.data.entity.InventarioBingoRegistradoEntity
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
                            inventariosNoRegistrados = sinRegistrar,
                            filteredInventarios = sinRegistrar.applyFilter(it.searchQuery),
                            totalInventariosNoRegistrados = sinRegistrar.size
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
                filteredInventarios = it.inventariosNoRegistrados.applyFilter(q)
            )
        }
    }

    fun registrarTodosComoNoEncontrados(actaUuid: UUID) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                val pendientes = _uiState.value.inventariosNoRegistrados
                if (pendientes.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@launch
                }
                pendientes.forEach { inventario ->
                    val entity = InventarioBingoRegistradoEntity(
                        uuidInventarioBingoRegistrado = UUID.randomUUID(),
                        uuidActa = actaUuid,
                        uuidInventario = inventario.uuidInventario,
                        codigoApuestaDiferente = false,
                        codigoApuestaDiferenteValor = null,
                        sillasDiferente = false,
                        sillasValor = null,
                        estado = EstadoInventarioEnum.NO_ENCONTRADO,
                        observaciones = "No encontrado"
                    )
                    inventarioBingoRegistradoDao.insert(entity)
                }
                _uiState.update { it.copy(isLoading = false, registroMasivoExitoso = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Error al registrar: ${e.message}")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun List<InventarioEntity>.applyFilter(query: String) =
        if (query.isBlank()) this
        else filter {
            it.tipoApuestaNombreInventario.lowercase().contains(query.lowercase()) ||
                    it.insCodigoInventario.lowercase().contains(query.lowercase())
        }

}