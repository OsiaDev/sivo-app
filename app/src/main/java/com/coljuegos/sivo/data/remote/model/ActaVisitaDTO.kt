package com.coljuegos.sivo.data.remote.model

import com.google.gson.annotations.SerializedName

data class ActaVisitaDTO(
    @SerializedName("nombrePresente")
    val nombrePresente: String?,

    @SerializedName("identificacionPresente")
    val identificacionPresente: String?,

    @SerializedName("municipio")
    val municipio: String?,

    @SerializedName("cargoPresente")
    val cargoPresente: String?,

    @SerializedName("emailPresente")
    val emailPresente: String?,

    @SerializedName("correosContacto")
    val correosContacto: String?
)