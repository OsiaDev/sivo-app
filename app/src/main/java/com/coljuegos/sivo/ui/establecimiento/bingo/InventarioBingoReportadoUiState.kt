package com.coljuegos.sivo.ui.establecimiento.bingo

import com.coljuegos.sivo.data.entity.InventarioBingoRegistradoEntity
import com.coljuegos.sivo.data.entity.InventarioEntity

data class InventarioBingoConRegistro(
    val inventario: InventarioEntity,
    val registro: InventarioBingoRegistradoEntity
)

data class InventarioBingoReportadoUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val registrados: List<InventarioBingoConRegistro> = emptyList(),
    val filteredRegistrados: List<InventarioBingoConRegistro> = emptyList(),
    val searchQuery: String = ""
)