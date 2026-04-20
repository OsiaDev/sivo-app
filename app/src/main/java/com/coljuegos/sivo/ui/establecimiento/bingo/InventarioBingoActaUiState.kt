package com.coljuegos.sivo.ui.establecimiento.bingo

import com.coljuegos.sivo.data.entity.InventarioEntity

data class InventarioBingoActaUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val inventariosNoRegistrados: List<InventarioEntity> = emptyList(),
    val filteredInventarios: List<InventarioEntity> = emptyList(),
    val totalInventariosNoRegistrados: Int = 0,
    val registroMasivoExitoso: Boolean = false,
    val searchQuery: String = ""
)