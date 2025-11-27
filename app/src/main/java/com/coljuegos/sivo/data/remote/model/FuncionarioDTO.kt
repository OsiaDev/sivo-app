package com.coljuegos.sivo.data.remote.model

import java.io.Serializable

data class FuncionarioDTO (
    val idUsuario: String? = null,
    val nombre: String? = null,
    val cargo: String? = null,
    val email: String? = null,
    val identificacion: String? = null
) : Serializable
