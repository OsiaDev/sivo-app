package com.coljuegos.sivo.ui.establecimiento.resumeninventario

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.dao.InventarioDao
import com.coljuegos.sivo.data.dao.InventarioRegistradoDao
import com.coljuegos.sivo.data.dao.NovedadRegistradaDao
import com.coljuegos.sivo.data.dao.ResumenInventarioDao
import com.coljuegos.sivo.data.dao.VerificacionContractualDao
import com.coljuegos.sivo.data.dao.VerificacionJuegoResponsableDao
import com.coljuegos.sivo.data.dao.VerificacionSiplaftDao
import com.coljuegos.sivo.data.entity.EstadoInventarioEnum
import com.coljuegos.sivo.data.entity.ResumenInventarioEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ResumenInventarioViewModel @Inject constructor(
    private val inventarioRegistradoDao: InventarioRegistradoDao,
    private val novedadRegistradaDao: NovedadRegistradaDao,
    private val resumenInventarioDao: ResumenInventarioDao,
    private val inventarioDao: InventarioDao,
    private val verificacionContractualDao: VerificacionContractualDao,
    private val verificacionJuegoResponsableDao: VerificacionJuegoResponsableDao,
    private val verificacionSiplaftDao: VerificacionSiplaftDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val actaUuid: UUID = checkNotNull(savedStateHandle.get<UUID>("actaUuid"))

    private val _uiState = MutableStateFlow(ResumenInventarioUiState())
    val uiState: StateFlow<ResumenInventarioUiState> = _uiState.asStateFlow()


    init {
        calcularEstadisticas()
        cargarNotas()
    }

    private fun calcularEstadisticas() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                // Obtener inventarios registrados
                inventarioRegistradoDao.getInventariosRegistradosByActa(actaUuid).collect { inventariosRegistrados ->

                    // Obtener novedades registradas
                    val novedadesRegistradas = novedadRegistradaDao.getNovedadesRegistradasByActaList(actaUuid)

                    // 1. Inventarios registrados operando
                    val inventariosOperando = inventariosRegistrados.count {
                        it.estado == EstadoInventarioEnum.OPERANDO
                    }

                    // 1.1 Inventarios registrados apagados
                    val inventariosApagados = inventariosRegistrados.count {
                        it.estado == EstadoInventarioEnum.APAGADO
                    }

                    // 2. Inventarios no encontrados
                    val inventariosNoEncontrados = inventariosRegistrados.count {
                        it.estado == EstadoInventarioEnum.NO_ENCONTRADO
                    }

                    // 3. Novedades sin placa
                    val novedadesSinPlaca = novedadesRegistradas.count { !it.tienePlaca }

                    // 4. Novedades con placa (total novedades - sin placa)
                    val novedadesConPlaca = novedadesRegistradas.size - novedadesSinPlaca

                    // 5. Total inventarios encontrados: (1 + 1.1 + 3 + 4)
                    val totalInventariosEncontrados = inventariosOperando + inventariosApagados +
                            novedadesSinPlaca +
                            novedadesConPlaca

                    // 6. Inventarios con código de apuesta diferente
                    val codigoApuestaDiferente = inventariosRegistrados.count {
                        it.codigoApuestaDiferente
                    }

                    // 7. Inventarios sin descripción de juego (sólo operando/apagado)
                    val inventariosSinDescripcionJuego = inventariosRegistrados.count {
                        (it.estado == EstadoInventarioEnum.OPERANDO || it.estado == EstadoInventarioEnum.APAGADO) && !it.descripcionJuego
                    }

                    // 8. Inventarios sin serial o no verificados
                    val inventariosSinSerial = inventariosRegistrados.count {
                        (it.estado == EstadoInventarioEnum.OPERANDO || it.estado == EstadoInventarioEnum.APAGADO) && !it.serialVerificado
                    }

                    // 9. Inventarios sin plan de premios
                    val inventariosSinPlanPremios = inventariosRegistrados.count {
                        (it.estado == EstadoInventarioEnum.OPERANDO || it.estado == EstadoInventarioEnum.APAGADO) && !it.planPremios
                    }

                    // 10. Inventarios sin valor de premios
                    val inventariosSinValorPremios = inventariosRegistrados.count {
                        (it.estado == EstadoInventarioEnum.OPERANDO || it.estado == EstadoInventarioEnum.APAGADO) && !it.valorPremios
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            inventariosOperando = inventariosOperando,
                            inventariosApagados = inventariosApagados,
                            inventariosNoEncontrados = inventariosNoEncontrados,
                            novedadesSinPlaca = novedadesSinPlaca,
                            novedadesConPlaca = novedadesConPlaca,
                            totalInventariosEncontrados = totalInventariosEncontrados,
                            codigoApuestaDiferente = codigoApuestaDiferente,
                            inventariosSinDescripcionJuego = inventariosSinDescripcionJuego,
                            inventariosSinSerial = inventariosSinSerial,
                            inventariosSinPlanPremios = inventariosSinPlanPremios,
                            inventariosSinValorPremios = inventariosSinValorPremios,
                            errorMessage = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al calcular estadísticas: ${e.message}"
                    )
                }
            }
        }
    }

    private fun cargarNotas() {
        viewModelScope.launch {
            val resumen = resumenInventarioDao.getResumenByActaId(actaUuid)
            _uiState.update { 
                it.copy(
                    notas = resumen?.notasResumen ?: "",
                    observacionesOperador = resumen?.observacionesOperador ?: ""
                ) 
            }
        }
    }

    fun guardarNotas(notas: String) {
        viewModelScope.launch {
            try {
                val existing = resumenInventarioDao.getResumenByActaId(actaUuid)
                if (existing != null) {
                    resumenInventarioDao.update(existing.copy(notasResumen = notas))
                } else {
                    resumenInventarioDao.insert(
                        ResumenInventarioEntity(uuidActa = actaUuid, notasResumen = notas)
                    )
                }
                _uiState.update { it.copy(notas = notas, guardadoExitoso = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error al guardar notas: ${e.message}") }
            }
        }
    }

    fun guardarObservacionesOperador(observacionesOperador: String) {
        viewModelScope.launch {
            try {
                val existing = resumenInventarioDao.getResumenByActaId(actaUuid)
                if (existing != null) {
                    resumenInventarioDao.update(existing.copy(observacionesOperador = observacionesOperador))
                } else {
                    resumenInventarioDao.insert(
                        ResumenInventarioEntity(uuidActa = actaUuid, observacionesOperador = observacionesOperador)
                    )
                }
                _uiState.update { it.copy(observacionesOperador = observacionesOperador, guardadoExitoso = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Error al guardar observaciones del operador: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun generarObservacionSugerida() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                val sugerencias = mutableListOf<String>()
                var numSugerencia = 1

                // 1. Contractual
                val contractual = verificacionContractualDao.getVerificacionContractualByActaId(actaUuid)
                if (contractual?.avisoAutorizacion == "No") {
                    sugerencias.add("$numSugerencia. El contrato del aviso de autorización no corresponde al autorizado, se debe instalar en lugar visible el aviso con el # del contrato correcto el cual debe cumplir con las especificaciones definidas en la resolución 20250007074 de 2025. Remitir foto como evidencia.")
                    numSugerencia++
                }

                // 2. Juego Responsable
                val juegoResponsable = verificacionJuegoResponsableDao.getVerificacionByActaId(actaUuid)
                if (juegoResponsable?.existenPiezasPublicitarias == "No" || juegoResponsable?.cuentaProgramaJuegoResponsable == "No") {
                    sugerencias.add("$numSugerencia. En cuanto a juego responsable no se tiene dispuestas piezas publicitarias, no se cuenta con el programa de juego. Se debe disponer en el casino de publicidad y el programa, enviando soportes a Coljuegos.")
                    numSugerencia++
                }

                // 3. SIPLAFT
                val siplaft = verificacionSiplaftDao.getVerificacionSiplaftByActaId(actaUuid)
                if (siplaft?.cuentaFormatoIdentificacion == "No" || siplaft?.cuentaFormatoReporteInterno == "No") {
                    sugerencias.add("$numSugerencia. No se cuenta con los formatos de SIPLAFT, se debe disponer la información en el casino y enviar evidencia a Coljuegos.")
                    numSugerencia++
                }

                // 4. Novedades no registradas
                val currentState = _uiState.value
                val totalNovedades = currentState.novedadesConPlaca + currentState.novedadesSinPlaca
                if (totalNovedades > 0) {
                    sugerencias.add("$numSugerencia. Se encontraron $totalNovedades mets no registradas no autorizadas para operar, se requiere actualizar el inventario.")
                    numSugerencia++
                }

                val inventariosRegistrados = inventarioRegistradoDao.getInventariosRegistradosByActaList(actaUuid)

                // 5. METs con diferencia en tipo de apuesta
                if (currentState.codigoApuestaDiferente > 0) {
                    val diferentes = inventariosRegistrados.filter { it.codigoApuestaDiferente }

                    val fabricantesAgrupados = mutableMapOf<String, Int>()
                    for (invRegistrado in diferentes) {
                        val inventarioOrigen = inventarioDao.getInventarioByUuid(invRegistrado.uuidInventario)
                        val fabricante = inventarioOrigen?.nombreMarcaInventario ?: "Desconocido"
                        fabricantesAgrupados[fabricante] = fabricantesAgrupados.getOrDefault(fabricante, 0) + 1
                    }

                    val builder = java.lang.StringBuilder()
                    builder.append("$numSugerencia. Se encontraron en total ${currentState.codigoApuestaDiferente} MET con diferencia en tipo de apuesta a las autorizadas. Al respecto se deben actualizar los tipos de apuesta.\n")
                    builder.append("Las ${currentState.codigoApuestaDiferente} MET están interconectadas en ${fabricantesAgrupados.size} grupos diferentes, así:\n")
                    
                    var indexFab = 1
                    for ((fab, count) in fabricantesAgrupados) {
                        builder.append("Grupo $indexFab $fab $count MET\n")
                        indexFab++
                    }
                    
                    sugerencias.add(builder.toString().trimEnd())
                    numSugerencia++
                }

                // 6. Resumen de inventarios con novedades (por serial)
                val maquinasSerialDiferente = mutableListOf<String>()
                val maquinasSinDescripcion = mutableListOf<String>()
                val maquinasSinPlanPremios = mutableListOf<String>()
                val maquinasSinValorPremios = mutableListOf<String>()
                val maquinasApagadas = mutableListOf<String>()
                val maquinasNoEncontradas = mutableListOf<String>()

                for (invRegistrado in inventariosRegistrados) {
                    val inventarioOrigen = inventarioDao.getInventarioByUuid(invRegistrado.uuidInventario)
                    val serial = inventarioOrigen?.metSerialInventario ?: "Desconocido"
                    
                    if ((invRegistrado.estado == EstadoInventarioEnum.OPERANDO || invRegistrado.estado == EstadoInventarioEnum.APAGADO)) {
                        if (!invRegistrado.serialVerificado) maquinasSerialDiferente.add(serial)
                        if (!invRegistrado.descripcionJuego) maquinasSinDescripcion.add(serial)
                        if (!invRegistrado.planPremios) maquinasSinPlanPremios.add(serial)
                        if (!invRegistrado.valorPremios) maquinasSinValorPremios.add(serial)
                    }
                    if (invRegistrado.estado == EstadoInventarioEnum.APAGADO) {
                        maquinasApagadas.add(serial)
                    }
                    if (invRegistrado.estado == EstadoInventarioEnum.NO_ENCONTRADO) {
                        maquinasNoEncontradas.add(serial)
                    }
                }

                val hasResumen = maquinasSerialDiferente.isNotEmpty() || maquinasSinDescripcion.isNotEmpty() || 
                                 maquinasSinPlanPremios.isNotEmpty() || maquinasSinValorPremios.isNotEmpty() ||
                                 maquinasApagadas.isNotEmpty() || maquinasNoEncontradas.isNotEmpty()
                
                if (hasResumen) {
                    sugerencias.add("") // Linea en blanco para separar
                    if (maquinasSerialDiferente.isNotEmpty()) {
                        sugerencias.add("Máquinas con serial diferente o no verificado: ${maquinasSerialDiferente.joinToString(", ")}")
                    }
                    if (maquinasSinDescripcion.isNotEmpty()) {
                        sugerencias.add("Máquinas sin descripción de juego: ${maquinasSinDescripcion.joinToString(", ")}")
                    }
                    if (maquinasSinPlanPremios.isNotEmpty()) {
                        sugerencias.add("Máquinas sin plan de premios: ${maquinasSinPlanPremios.joinToString(", ")}")
                    }
                    if (maquinasSinValorPremios.isNotEmpty()) {
                        sugerencias.add("Máquinas sin valor de premios: ${maquinasSinValorPremios.joinToString(", ")}")
                    }
                    if (maquinasApagadas.isNotEmpty()) {
                        sugerencias.add("Máquinas apagadas: ${maquinasApagadas.joinToString(", ")}")
                    }
                    if (maquinasNoEncontradas.isNotEmpty()) {
                        sugerencias.add("Máquinas no encontradas: ${maquinasNoEncontradas.joinToString(", ")}")
                    }
                }

                if (sugerencias.isNotEmpty()) {
                    val textoFinal = sugerencias.joinToString("\n")
                    guardarNotas(textoFinal)
                }

                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al generar sugerida: ${e.message}"
                    ) 
                }
            }
        }
    }
}