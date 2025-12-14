package com.coljuegos.sivo.ui.home

import com.coljuegos.sivo.data.entity.ActaEntity

data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val userFullName: String? = null,
    val actas: List<ActaEntity> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)