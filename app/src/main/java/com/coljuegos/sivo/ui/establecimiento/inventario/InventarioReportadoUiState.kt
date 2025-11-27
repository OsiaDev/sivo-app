package com.coljuegos.sivo.ui.establecimiento.inventario

import com.coljuegos.sivo.data.entity.InventarioEntity
import com.coljuegos.sivo.data.entity.InventarioRegistradoEntity

/**
 * Clase que combina un inventario con su registro (si existe)
 */
data class InventarioConRegistro(
    val inventario: InventarioEntity,
    val registro: InventarioRegistradoEntity?
)

data class InventarioReportadoUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val inventariosRegistrados: List<InventarioConRegistro> = emptyList(),
    val totalInventariosRegistrados: Int = 0
)