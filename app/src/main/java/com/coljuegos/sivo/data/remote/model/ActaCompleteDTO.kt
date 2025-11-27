package com.coljuegos.sivo.data.remote.model

import com.google.gson.annotations.SerializedName

data class ActaCompleteDTO(
    @SerializedName("numActa")
    val numActa: Int,

    @SerializedName("latitud")
    val latitud: Double,

    @SerializedName("longitud")
    val longitud: Double,

    @SerializedName("actaVisita")
    val actaVisita: ActaVisitaDTO?,

    @SerializedName("verificacionContractual")
    val verificacionContractual: VerificacionContractualDTO?,

    @SerializedName("verificacionSiplaft")
    val verificacionSiplaft: VerificacionSiplaftDTO?,

    @SerializedName("inventariosRegistrados")
    val inventariosRegistrados: List<InventarioRegistradoDTO>?,

    @SerializedName("novedadesRegistradas")
    val novedadesRegistradas: List<NovedadRegistradaDTO>?,

    @SerializedName("firmaActa")
    val firmaActa: FirmaActaDTO?,

    @SerializedName("imagenes")
    val imagenes: List<ImagenDTO>?
)