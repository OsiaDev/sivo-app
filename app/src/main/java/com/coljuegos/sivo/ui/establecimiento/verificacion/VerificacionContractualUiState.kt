package com.coljuegos.sivo.ui.establecimiento.verificacion

import java.util.UUID

data class VerificacionContractualUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    val actaUuid: UUID? = null,

    // Opciones disponibles para los spinners
    val opcionesSiNoNa: List<String> = listOf("Si", "No", "N/A"),
    val opcionesTipoActividad: List<String> = listOf("Bar", "Billares", "Tienda", "Otros"),

    // Respuestas seleccionadas
    val avisoAutorizacion: String = "",
    val direccionCorresponde: String = "",
    val otraDireccion: String = "",
    val nombreEstablecimientoCorresponde: String = "",
    val otroNombre: String = "",
    val desarrollaActividadesDiferentes: String = "",
    val tipoActividad: String = "",
    val especificacionOtros: String = "",
    val cuentaRegistrosMantenimiento: String = "",

    // Estados de UI
    val mostrarCampoOtraDireccion: Boolean = false,
    val mostrarCampoOtroNombre: Boolean = false,
    val mostrarSeccionActividadesDiferentes: Boolean = false,
    val mostrarCampoOtros: Boolean = false
)