package com.coljuegos.sivo.data.remote.model

import com.google.gson.annotations.SerializedName

data class LoginRequestDTO(

    @SerializedName("username")
    val username: String,

    @SerializedName("password")
    val password: String

)
