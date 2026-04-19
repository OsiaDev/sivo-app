package com.coljuegos.sivo.ui.establecimiento.bingo

import com.coljuegos.sivo.data.entity.InventarioBingoRegistradoEntity
import com.coljuegos.sivo.data.entity.InventarioEntity

data class RegistrarBingoUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val inventario: InventarioEntity? = null,
    val registro: InventarioBingoRegistradoEntity? = null,
    val esEdicion: Boolean = false,
    val guardadoExitoso: Boolean = false
)