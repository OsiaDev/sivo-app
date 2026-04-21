package com.coljuegos.sivo.data.remote.model

import com.google.gson.annotations.SerializedName

data class VerificacionBingoDTO(

    @SerializedName("cartonesModulos")
    val cartonesModulos: String?,

    @SerializedName("sistemaTecnologico")
    val sistemaTecnologico: String?,

    @SerializedName("sistemaInterconectado")
    val sistemaInterconectado: String?,

    @SerializedName("realizaEventosEspeciales")
    val realizaEventosEspeciales: String?,

    @SerializedName("tipoBalotera")
    val tipoBalotera: String?,

    @SerializedName("valorCartonExpuesto")
    val valorCartonExpuesto: String?
    
)
