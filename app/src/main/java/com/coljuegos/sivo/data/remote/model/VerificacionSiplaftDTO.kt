package com.coljuegos.sivo.data.remote.model

import com.google.gson.annotations.SerializedName

data class VerificacionSiplaftDTO(
    @SerializedName("cuentaFormatoIdentificacion")
    val cuentaFormatoIdentificacion: String?,

    @SerializedName("montoIdentificacion")
    val montoIdentificacion: String?,

    @SerializedName("cuentaFormatoReporteInterno")
    val cuentaFormatoReporteInterno: String?,

    @SerializedName("senalesAlerta")
    val senalesAlerta: String?,

    @SerializedName("conoceCodigoConducta")
    val conoceCodigoConducta: String?
)