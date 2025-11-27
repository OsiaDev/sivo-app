package com.coljuegos.sivo.data.remote.model

data class MaestrosResponseDTO(
    val tiposApuesta: List<TipoApuestaDTO>,
    val ultimaActualizacion: String? = null
)