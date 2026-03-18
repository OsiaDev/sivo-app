package com.coljuegos.sivo.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.dao.ActaDao
import com.coljuegos.sivo.data.dao.ImagenDao
import com.coljuegos.sivo.data.entity.ActaEntity
import com.coljuegos.sivo.data.entity.ActaStateEnum
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ResumenSincronizacionUiState(
    val acta: ActaEntity? = null,
    val isActaSincronizada: Boolean = false,
    val countSincronizadas: Int = 0,
    val countPendientes: Int = 0,
    val countErrores: Int = 0,
    val isLoading: Boolean = false
)

@HiltViewModel
class ResumenSincronizacionViewModel @Inject constructor(
    private val actaDao: ActaDao,
    private val imagenDao: ImagenDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResumenSincronizacionUiState())
    val uiState = _uiState.asStateFlow()

    fun cargarDatos(actaUuid: UUID) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val acta = actaDao.getActaByUuid(actaUuid)
            val syncCount = imagenDao.getSynchronizedImagenesCountByActa(actaUuid)
            val pendingCount = imagenDao.getPendingImagenesCountByActa(actaUuid)
            val failedCount = imagenDao.getFailedImagenesCountByActa(actaUuid)
            
            _uiState.value = ResumenSincronizacionUiState(
                acta = acta,
                isActaSincronizada = acta?.stateActa == ActaStateEnum.SINCRONIZADO,
                countSincronizadas = syncCount,
                countPendientes = pendingCount,
                countErrores = failedCount,
                isLoading = false
            )
        }
    }
}
