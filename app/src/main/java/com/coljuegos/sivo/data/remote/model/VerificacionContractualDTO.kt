package com.coljuegos.sivo.data.remote.model

import com.google.gson.annotations.SerializedName

data class VerificacionContractualDTO(
    @SerializedName("avisoAutorizacion")
    val avisoAutorizacion: String?,

    @SerializedName("direccionCorresponde")
    val direccionCorresponde: String?,

    @SerializedName("otraDireccion")
    val otraDireccion: String?,

    @SerializedName("nombreEstablecimientoCorresponde")
    val nombreEstablecimientoCorresponde: String?,

    @SerializedName("otroNombre")
    val otroNombre: String?,

    @SerializedName("desarrollaActividadesDiferentes")
    val desarrollaActividadesDiferentes: String?,

    @SerializedName("tipoActividad")
    val tipoActividad: String?,

    @SerializedName("especificacionOtros")
    val especificacionOtros: String?,

    @SerializedName("cuentaRegistrosMantenimiento")
    val cuentaRegistrosMantenimiento: String?
)