package com.coljuegos.sivo.data.remote.model

import com.google.gson.annotations.SerializedName

data class VerificacionJuegoResponsableDTO(

    @SerializedName("cuentaTestIdentificacionRiesgos")
    val cuentaTestIdentificacionRiesgos: String?,

    @SerializedName("existenPiezasPublicitarias")
    val existenPiezasPublicitarias: String?,

    @SerializedName("cuentaProgramaJuegoResponsable")
    val cuentaProgramaJuegoResponsable: String?
)