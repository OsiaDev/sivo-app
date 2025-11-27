package com.coljuegos.sivo.data.remote.model

import com.google.gson.annotations.SerializedName

data class LoginResponseDTO(

    @SerializedName("token")
    val token: String,

    @SerializedName("tokenType")
    val tokenType: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("user")
    val user: UserInfoDTO

)
