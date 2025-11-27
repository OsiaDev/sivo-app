package com.coljuegos.sivo.data.remote.model

import java.io.Serializable

data class ActaResponseDTO (
    val actas: List<ActaDTO>
) : Serializable
