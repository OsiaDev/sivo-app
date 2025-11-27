package com.coljuegos.sivo.data.remote.model

import com.google.gson.annotations.SerializedName

data class ActaSincronizacionResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String?,

    @SerializedName("numActa")
    val numActa: Int?
)