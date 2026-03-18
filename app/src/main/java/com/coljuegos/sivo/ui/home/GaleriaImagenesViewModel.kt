package com.coljuegos.sivo.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.dao.ImagenDao
import com.coljuegos.sivo.data.entity.ImagenEntity
import com.coljuegos.sivo.workers.ImagenSincronizacionWorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ImagenItemUiModel(
    val entity: ImagenEntity,
    val isSelected: Boolean
)

data class GaleriaUiState(
    val items: List<ImagenItemUiModel> = emptyList(),
    val isLoading: Boolean = false,
    val canSync: Boolean = false
)

@HiltViewModel
class GaleriaImagenesViewModel @Inject constructor(
    private val imagenDao: ImagenDao,
    private val syncWorkManager: ImagenSincronizacionWorkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(GaleriaUiState())
    val uiState: StateFlow<GaleriaUiState> = _uiState.asStateFlow()

    private var currentActaUuid: UUID? = null
    private val _selectedUuids = MutableStateFlow<Set<UUID>>(emptySet())

    fun cargarImagenes(actaUuidStr: String, filtro: String) {
        val actaUuid = UUID.fromString(actaUuidStr)
        currentActaUuid = actaUuid
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val flow = when (filtro) {
                "CARGADAS" -> imagenDao.getSynchronizedImagenesByActaFlow(actaUuid)
                "ERRORES" -> imagenDao.getFailedImagenesByActaFlow(actaUuid)
                else -> imagenDao.getPendingImagenesByActaFlow(actaUuid) // PENDIENTES
            }

            kotlinx.coroutines.flow.combine(flow, _selectedUuids) { list, selected ->
                list.map { ImagenItemUiModel(it, it.uuidImagen in selected) }
            }.collectLatest { uiItems ->
                _uiState.value = _uiState.value.copy(
                    items = uiItems,
                    isLoading = false,
                    canSync = filtro != "CARGADAS" && uiItems.any { it.isSelected }
                )
            }
        }
    }

    fun toggleSeleccion(uuid: UUID) {
        val current = _selectedUuids.value.toMutableSet()
        if (current.contains(uuid)) {
            current.remove(uuid)
        } else {
            current.add(uuid)
        }
        _selectedUuids.value = current
    }

    fun seleccionarTodas() {
        val allUuids = _uiState.value.items.map { it.entity.uuidImagen }.toSet()
        _selectedUuids.value = allUuids
    }

    fun deseleccionarTodas() {
        _selectedUuids.value = emptySet()
    }

    fun iniciarSincronizacion() {
        val selected = _selectedUuids.value
        if (selected.isEmpty()) return

        viewModelScope.launch {
            // Si el worker sube todo lo pendiente, y queremos solo seleccionadas:
            // Tendríamos que marcar las seleccionadas para subir o pasar la lista.
            // Dada la limitación del worker actual, dispararemos la sincronización general
            // pero el usuario solo "activó" el botón con su selección.
            
            // Si el backend/worker permite IDs específicos, lo ideal sería pasarlos.
            syncWorkManager.ejecutarSincronizacionInmediata()
            deseleccionarTodas()
        }
    }
}
