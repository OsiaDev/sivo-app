package com.coljuegos.sivo.data.remote.model

import java.io.Serializable

data class InventarioDTO (
    val nombreMarca: String? = null,
    val metSerial: String? = null,
    val insCodigo: String? = null,
    val invSillas: Int? = null,
    val tipoApuestaNombre: String? = null,
    val metOnline: Boolean? = null,
    val codigoTipoApuesta: String? = null,
    val nuc: String? = null,
    val conCodigo: Long? = null,
    val aucNumero: Int? = null,
    val estCodigo: Long? = null
) : Serializable
