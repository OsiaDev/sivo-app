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

    // Campos de verificación de código de apuesta
    @SerializedName("codigoApuestaDiferente")
    val codigoApuestaDiferente: Boolean,

    @SerializedName("codigoApuestaDiferenteValor")
    val codigoApuestaDiferenteValor: String?,

    // Campos de verificación de serial
    @SerializedName("serialVerificado")
    val serialVerificado: Boolean,

    @SerializedName("serialDiferente")
    val serialDiferente: String?,

    // Campos de verificación de características
    @SerializedName("descripcionJuego")
    val descripcionJuego: Boolean,

    @SerializedName("planPremios")
    val planPremios: Boolean,

    @SerializedName("valorPremios")
    val valorPremios: Boolean,

    @SerializedName("valorCredito")
    val valorCredito: String?,

    // Campo de verificación de contadores
    @SerializedName("contadoresVerificado")
    val contadoresVerificado: Boolean,

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