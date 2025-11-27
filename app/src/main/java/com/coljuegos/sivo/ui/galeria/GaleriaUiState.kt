package com.coljuegos.sivo.ui.galeria

import com.coljuegos.sivo.data.entity.ImagenEntity

data class GaleriaUiState(
    val imagenes: List<ImagenEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
