package com.coljuegos.sivo.data.remote.model

import com.google.gson.annotations.SerializedName

data class ResumenInventarioDTO(

    @SerializedName("notasResumen")
    val notasResumen: String?,

    @SerializedName("observacionesOperador")
    val observacionesOperador: String?

)