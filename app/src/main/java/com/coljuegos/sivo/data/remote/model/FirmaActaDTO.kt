package com.coljuegos.sivo.data.remote.model

import com.google.gson.annotations.SerializedName

data class FirmaActaDTO(
    @SerializedName("nombreFiscalizadorPrincipal")
    val nombreFiscalizadorPrincipal: String?,

    @SerializedName("ccFiscalizadorPrincipal")
    val ccFiscalizadorPrincipal: String?,

    @SerializedName("cargoFiscalizadorPrincipal")
    val cargoFiscalizadorPrincipal: String?,

    @SerializedName("firmaFiscalizadorPrincipal")
    val firmaFiscalizadorPrincipal: String?,

    @SerializedName("nombreFiscalizadorSecundario")
    val nombreFiscalizadorSecundario: String?,

    @SerializedName("ccFiscalizadorSecundario")
    val ccFiscalizadorSecundario: String?,

    @SerializedName("cargoFiscalizadorSecundario")
    val cargoFiscalizadorSecundario: String?,

    @SerializedName("firmaFiscalizadorSecundario")
    val firmaFiscalizadorSecundario: String?,

    @SerializedName("nombreOperador")
    val nombreOperador: String?,

    @SerializedName("ccOperador")
    val ccOperador: String?,

    @SerializedName("cargoOperador")
    val cargoOperador: String?,

    @SerializedName("firmaOperador")
    val firmaOperador: String?
)