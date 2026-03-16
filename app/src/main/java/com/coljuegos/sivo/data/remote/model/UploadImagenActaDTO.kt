package com.coljuegos.sivo.data.remote.model

import com.google.gson.annotations.SerializedName

data class UploadImagenActaDTO(
    @SerializedName("numActa")
    val numActa: Int,
    @SerializedName("imagen")
    val imagen: ImagenDTO
)
