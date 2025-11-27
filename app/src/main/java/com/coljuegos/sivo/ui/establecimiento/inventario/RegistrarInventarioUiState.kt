package com.coljuegos.sivo.ui.establecimiento.inventario

import com.coljuegos.sivo.data.entity.InventarioEntity
import com.coljuegos.sivo.data.entity.InventarioRegistradoEntity

data class RegistrarInventarioUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val inventario: InventarioEntity? = null,
    val inventarioRegistrado: InventarioRegistradoEntity? = null,
    val esEdicion: Boolean = false,
    val guardadoExitoso: Boolean = false
)