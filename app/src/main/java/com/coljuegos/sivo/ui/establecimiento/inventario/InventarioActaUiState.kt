package com.coljuegos.sivo.ui.establecimiento.inventario

import com.coljuegos.sivo.data.entity.InventarioEntity

data class InventarioActaUiState(
    val inventariosNoRegistrados: List<InventarioEntity> = emptyList(),
    val filteredInventarios: List<InventarioEntity> = emptyList(),
    val totalInventariosNoRegistrados: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = ""
)