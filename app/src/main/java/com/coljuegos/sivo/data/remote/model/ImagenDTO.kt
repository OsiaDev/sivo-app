package com.coljuegos.sivo.data.remote.model

import com.google.gson.annotations.SerializedName

data class ImagenDTO(
    @SerializedName("nombreImagen")
    val nombreImagen: String,

    @SerializedName("imagenBase64")
    val imagenBase64: String,

    @SerializedName("descripcion")
    val descripcion: String?,

    @SerializedName("fragmentOrigen")
    val fragmentOrigen: String?
)