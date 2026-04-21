package com.coljuegos.sivo.data.remote.model

import com.google.gson.annotations.SerializedName

data class InventarioBingoRegistradoDTO(

    @SerializedName("serial")
    val serial: String,

    @SerializedName("marca")
    val marca: String?,

    @SerializedName("codigoApuesta")
    val codigoApuesta: String?,

    @SerializedName("estado")
    val estado: String?,

    @SerializedName("codigoApuestaDiferente")
    val codigoApuestaDiferente: Boolean,

    @SerializedName("codigoApuestaDiferenteValor")
    val codigoApuestaDiferenteValor: String?,

    @SerializedName("sillasDiferente")
    val sillasDiferente: Boolean,

    @SerializedName("sillasValor")
    val sillasValor: Int?,

    @SerializedName("sillasOriginal")
    val sillasOriginal: Int?,

    @SerializedName("observaciones")
    val observaciones: String?
    
)
