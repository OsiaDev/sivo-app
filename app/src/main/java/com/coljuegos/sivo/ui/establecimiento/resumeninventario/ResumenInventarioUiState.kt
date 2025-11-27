package com.coljuegos.sivo.ui.establecimiento.resumeninventario

data class ResumenInventarioUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // Métricas de inventario
    val inventariosOperandoApagado: Int = 0,  // (1) Registrados operando o apagado
    val inventariosNoEncontrados: Int = 0,    // (2) No encontrados
    val novedadesSinPlaca: Int = 0,           // (3) Novedades sin placa
    val novedadesConPlaca: Int = 0,           // (4) Novedades con placa (total - sin placa)
    val totalInventariosEncontrados: Int = 0, // (5) Fórmula: 1 - 2 + 3 + 4
    val codigoApuestaDiferente: Int = 0       // (6) Inventarios con código diferente
)