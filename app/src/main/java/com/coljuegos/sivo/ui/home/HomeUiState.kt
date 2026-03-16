package com.coljuegos.sivo.ui.home

import com.coljuegos.sivo.data.entity.ActaEntity

data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val userFullName: String? = null,
    val pendientes: List<ActaEntity> = emptyList(),
    val completadas: List<ActaEntity> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)