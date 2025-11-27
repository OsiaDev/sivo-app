package com.coljuegos.sivo.data.remote.model

import java.io.Serializable

data class DireccionDTO (
    val direccion: String? = null,
    val establecimiento: String? = null,
    val estCodigo: String? = null,
    val ciudad: String? = null,
    val departamento: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null
) : Serializable
