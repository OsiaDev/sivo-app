package com.coljuegos.sivo.data.remote.model

import com.google.gson.annotations.SerializedName

data class UserInfoDTO(

    @SerializedName("idUser")
    val idUser: String,

    @SerializedName("nameUser")
    val nameUser: String,

    @SerializedName("fullNameUser")
    val fullNameUser: String,

    @SerializedName("emailUser")
    val emailUser: String,

    @SerializedName("perCodigo")
    val perCodigoUser: Long

)
