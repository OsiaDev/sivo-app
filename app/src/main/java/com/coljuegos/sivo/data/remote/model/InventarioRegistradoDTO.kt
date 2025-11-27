package com.coljuegos.sivo.data.remote.model

import com.google.gson.annotations.SerializedName

data class InventarioRegistradoDTO(
    @SerializedName("serial")
    val serial: String,

    @SerializedName("marca")
    val marca: String,

    @SerializedName("codigoApuesta")
    val codigoApuesta: String,

    @SerializedName("estado")
    val estado: String,

    @SerializedName("valorCredito")
    val valorCredito: String?,

    @SerializedName("coinInMet")
    val coinInMet: String?,

    @SerializedName("coinOutMet")
    val coinOutMet: String?,

    @SerializedName("jackpotMet")
    val jackpotMet: String?,

    @SerializedName("coinInSclm")
    val coinInSclm: String?,

    @SerializedName("coinOutSclm")
    val coinOutSclm: String?,

    @SerializedName("jackpotSclm")
    val jackpotSclm: String?,

    @SerializedName("observaciones")
    val observaciones: String?
)