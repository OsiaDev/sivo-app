package com.coljuegos.sivo.ui.home

import com.coljuegos.sivo.data.entity.ActaEntity

data class ActaCompletadaUiModel(
    val acta: ActaEntity,
    val todoSincronizado: Boolean
)
