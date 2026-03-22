package com.coljuegos.sivo.ui.establecimiento.resumeninventario

data class ResumenInventarioUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // Métricas de inventario
    val inventariosOperando: Int = 0,         // (1) Registrados operando
    val inventariosApagados: Int = 0,         // (1.1) Registrados apagado
    val inventariosNoEncontrados: Int = 0,    // (2) No encontrados
    val novedadesSinPlaca: Int = 0,           // (3) Novedades sin placa
    val novedadesConPlaca: Int = 0,           // (4) Novedades con placa (total - sin placa)
    val totalInventariosEncontrados: Int = 0, // (5) Fórmula: 1 + 3 + 4
    val codigoApuestaDiferente: Int = 0,       // (6) Inventarios con código diferente
    val inventariosSinDescripcionJuego: Int = 0, // (7) Inventarios sin descripción de juego
    val notas: String = "",
    val observacionesOperador: String = "",
    val guardadoExitoso: Boolean = false
)