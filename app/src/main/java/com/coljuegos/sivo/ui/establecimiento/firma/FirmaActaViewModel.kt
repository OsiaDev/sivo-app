package com.coljuegos.sivo.ui.establecimiento.firma

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.dao.ActaVisitaDao
import com.coljuegos.sivo.data.dao.FirmaActaDao
import com.coljuegos.sivo.data.dao.FuncionarioDao
import com.coljuegos.sivo.data.entity.FirmaActaEntity
import com.coljuegos.sivo.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class FirmaActaViewModel @Inject constructor(
    private val firmaActaDao: FirmaActaDao,
    private val funcionarioDao: FuncionarioDao,
    private val actaVisitaDao: ActaVisitaDao,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val actaUuid: UUID = checkNotNull(savedStateHandle.get<UUID>("actaUuid"))

    private val _uiState = MutableStateFlow(FirmaActaUiState())
    val uiState: StateFlow<FirmaActaUiState> = _uiState.asStateFlow()

    // StateFlows para las firmas individuales (usados por SignatureViewModel)
    private val _firmaPrincipalBitmap = MutableStateFlow<Bitmap?>(null)
    val firmaPrincipalBitmap: StateFlow<Bitmap?> = _firmaPrincipalBitmap.asStateFlow()

    private val _firmaSecundarioBitmap = MutableStateFlow<Bitmap?>(null)
    val firmaSecundarioBitmap: StateFlow<Bitmap?> = _firmaSecundarioBitmap.asStateFlow()

    private val _firmaOperadorBitmap = MutableStateFlow<Bitmap?>(null)
    val firmaOperadorBitmap: StateFlow<Bitmap?> = _firmaOperadorBitmap.asStateFlow()

    init {
        loadFirmaActa()
    }

    fun loadFirmaActa() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                firmaActaDao.getFirmaActaByActaUuid(actaUuid).collect { firmaActa ->
                    if (firmaActa != null) {
                        var opNombre = firmaActa.nombreOperador ?: ""
                        var opCc = firmaActa.ccOperador ?: ""
                        var opCargo = firmaActa.cargoOperador ?: ""
                        
                        // Si ya existe registro de firma, pero el operador esta vacio, intentamos precargarlo
                        if (opNombre.isBlank() && opCc.isBlank()) {
                            val actaVisita = withContext(Dispatchers.IO) { actaVisitaDao.getActaVisitaByActaId(actaUuid) }
                            if (actaVisita != null) {
                                opNombre = actaVisita.nombrePresente ?: ""
                                opCc = actaVisita.identificacionPresente ?: ""
                                opCargo = actaVisita.cargoPresente ?: ""
                            }
                        }

                        val principalBitmap = withContext(Dispatchers.IO) {
                            firmaActa.firmaFiscalizadorPrincipal?.let { base64ToBitmap(it) }
                        }
                        val secundarioBitmap = withContext(Dispatchers.IO) {
                            firmaActa.firmaFiscalizadorSecundario?.let { base64ToBitmap(it) }
                        }
                        val operadorBitmap = withContext(Dispatchers.IO) {
                            firmaActa.firmaOperador?.let { base64ToBitmap(it) }
                        }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                nombreFiscalizadorPrincipal = firmaActa.nombreFiscalizadorPrincipal ?: "",
                                ccFiscalizadorPrincipal = firmaActa.ccFiscalizadorPrincipal ?: "",
                                cargoFiscalizadorPrincipal = firmaActa.cargoFiscalizadorPrincipal ?: "",
                                firmaFiscalizadorPrincipal = principalBitmap,

                                nombreFiscalizadorSecundario = firmaActa.nombreFiscalizadorSecundario ?: "",
                                ccFiscalizadorSecundario = firmaActa.ccFiscalizadorSecundario ?: "",
                                cargoFiscalizadorSecundario = firmaActa.cargoFiscalizadorSecundario ?: "",
                                firmaFiscalizadorSecundario = secundarioBitmap,

                                nombreOperador = opNombre,
                                ccOperador = opCc,
                                cargoOperador = opCargo,
                                firmaOperador = operadorBitmap
                            )
                        }

                        // Actualizar los StateFlows de las firmas
                        _firmaPrincipalBitmap.value = _uiState.value.firmaFiscalizadorPrincipal
                        _firmaSecundarioBitmap.value = _uiState.value.firmaFiscalizadorSecundario
                        _firmaOperadorBitmap.value = _uiState.value.firmaOperador
                    } else {
                        preRellenarDesdeFuncionarios()
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar las firmas: ${e.message}"
                    )
                }
            }
        }
    }

    private fun preRellenarDesdeFuncionarios() {
        viewModelScope.launch {
            try {
                val session = sessionManager.getCurrentSession()
                val funcionarios = withContext(Dispatchers.IO) { funcionarioDao.getFuncionariosByActa(actaUuid) }
                val actaVisita = withContext(Dispatchers.IO) { actaVisitaDao.getActaVisitaByActaId(actaUuid) }

                // Principal: el funcionario cuyo idUsuario coincide con el usuario logueado
                val principal = funcionarios.find { it.idUsuarioFuncionario == session?.idUserSession }

                // Secundario: el primero que NO sea el principal
                val secundario = funcionarios.firstOrNull { it.idUsuarioFuncionario != session?.idUserSession }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        nombreFiscalizadorPrincipal = principal?.nombreFuncionario ?: "",
                        ccFiscalizadorPrincipal = principal?.identificacionFuncionario ?: "",
                        cargoFiscalizadorPrincipal = principal?.cargoFuncionario ?: "",
                        nombreFiscalizadorSecundario = secundario?.nombreFuncionario ?: "",
                        ccFiscalizadorSecundario = secundario?.identificacionFuncionario ?: "",
                        cargoFiscalizadorSecundario = secundario?.cargoFuncionario ?: "",
                        // Precargando la informacion del operador (Atendido Por)
                        nombreOperador = actaVisita?.nombrePresente ?: "",
                        ccOperador = actaVisita?.identificacionPresente ?: "",
                        cargoOperador = actaVisita?.cargoPresente ?: ""
                    )
                }

                // Persistir inmediatamente para que doOnTextChanged no lo sobreescriba con vacío
                saveFirmaActaData()

            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateNombreFiscalizadorPrincipal(nombre: String) {
        _uiState.update { it.copy(nombreFiscalizadorPrincipal = nombre) }
        saveFirmaActaData()
    }

    fun updateCcFiscalizadorPrincipal(cc: String) {
        _uiState.update { it.copy(ccFiscalizadorPrincipal = cc) }
        saveFirmaActaData()
    }

    fun updateCargoFiscalizadorPrincipal(cargo: String) {
        _uiState.update { it.copy(cargoFiscalizadorPrincipal = cargo) }
        saveFirmaActaData()
    }

    fun updateNombreFiscalizadorSecundario(nombre: String) {
        _uiState.update { it.copy(nombreFiscalizadorSecundario = nombre) }
        saveFirmaActaData()
    }

    fun updateCcFiscalizadorSecundario(cc: String) {
        _uiState.update { it.copy(ccFiscalizadorSecundario = cc) }
        saveFirmaActaData()
    }

    fun updateCargoFiscalizadorSecundario(cargo: String) {
        _uiState.update { it.copy(cargoFiscalizadorSecundario = cargo) }
        saveFirmaActaData()
    }

    fun updateNombreOperador(nombre: String) {
        _uiState.update { it.copy(nombreOperador = nombre) }
        saveFirmaActaData()
    }

    fun updateCcOperador(cc: String) {
        _uiState.update { it.copy(ccOperador = cc) }
        saveFirmaActaData()
    }

    fun updateCargoOperador(cargo: String) {
        _uiState.update { it.copy(cargoOperador = cargo) }
        saveFirmaActaData()
    }

    // NUEVO: Método para guardar automáticamente sin validaciones
    private fun saveFirmaActaData() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value

                // Buscar si ya existe un registro
                val existingFirmaActa = withContext(Dispatchers.IO) { firmaActaDao.getFirmaActaByActaUuidSuspend(actaUuid) }

                val base64Principal = withContext(Dispatchers.IO) { currentState.firmaFiscalizadorPrincipal?.let { bitmapToBase64(it) } }
                val base64Secundario = withContext(Dispatchers.IO) { currentState.firmaFiscalizadorSecundario?.let { bitmapToBase64(it) } }
                val base64Operador = withContext(Dispatchers.IO) { currentState.firmaOperador?.let { bitmapToBase64(it) } }

                val firmaActaToSave = existingFirmaActa?.copy(
                    nombreFiscalizadorPrincipal = currentState.nombreFiscalizadorPrincipal.takeIf { it.isNotBlank() },
                    ccFiscalizadorPrincipal = currentState.ccFiscalizadorPrincipal.takeIf { it.isNotBlank() },
                    cargoFiscalizadorPrincipal = currentState.cargoFiscalizadorPrincipal.takeIf { it.isNotBlank() },
                    firmaFiscalizadorPrincipal = base64Principal,

                    nombreFiscalizadorSecundario = currentState.nombreFiscalizadorSecundario.takeIf { it.isNotBlank() },
                    ccFiscalizadorSecundario = currentState.ccFiscalizadorSecundario.takeIf { it.isNotBlank() },
                    cargoFiscalizadorSecundario = currentState.cargoFiscalizadorSecundario.takeIf { it.isNotBlank() },
                    firmaFiscalizadorSecundario = base64Secundario,

                    nombreOperador = currentState.nombreOperador.takeIf { it.isNotBlank() },
                    ccOperador = currentState.ccOperador.takeIf { it.isNotBlank() },
                    cargoOperador = currentState.cargoOperador.takeIf { it.isNotBlank() },
                    firmaOperador = base64Operador
                ) ?: FirmaActaEntity(
                    uuidActa = actaUuid,
                    nombreFiscalizadorPrincipal = currentState.nombreFiscalizadorPrincipal.takeIf { it.isNotBlank() },
                    ccFiscalizadorPrincipal = currentState.ccFiscalizadorPrincipal.takeIf { it.isNotBlank() },
                    cargoFiscalizadorPrincipal = currentState.cargoFiscalizadorPrincipal.takeIf { it.isNotBlank() },
                    firmaFiscalizadorPrincipal = base64Principal,

                    nombreFiscalizadorSecundario = currentState.nombreFiscalizadorSecundario.takeIf { it.isNotBlank() },
                    ccFiscalizadorSecundario = currentState.ccFiscalizadorSecundario.takeIf { it.isNotBlank() },
                    cargoFiscalizadorSecundario = currentState.cargoFiscalizadorSecundario.takeIf { it.isNotBlank() },
                    firmaFiscalizadorSecundario = base64Secundario,

                    nombreOperador = currentState.nombreOperador.takeIf { it.isNotBlank() },
                    ccOperador = currentState.ccOperador.takeIf { it.isNotBlank() },
                    cargoOperador = currentState.cargoOperador.takeIf { it.isNotBlank() },
                    firmaOperador = base64Operador
                )

                withContext(Dispatchers.IO) { firmaActaDao.insertFirmaActa(firmaActaToSave) }
            } catch (e: Exception) {
                // Error silencioso, no interrumpir la escritura del usuario
            }
        }
    }

    fun saveFirmaPrincipal(bitmap: Bitmap) {
        _firmaPrincipalBitmap.value = bitmap
        _uiState.update { it.copy(firmaFiscalizadorPrincipal = bitmap) }
        saveFirmaActaData()
    }

    fun saveFirmaSecundario(bitmap: Bitmap) {
        _firmaSecundarioBitmap.value = bitmap
        _uiState.update { it.copy(firmaFiscalizadorSecundario = bitmap) }
        saveFirmaActaData()
    }

    fun saveFirmaOperador(bitmap: Bitmap) {
        _firmaOperadorBitmap.value = bitmap
        _uiState.update { it.copy(firmaOperador = bitmap) }
        saveFirmaActaData()
    }

    fun saveFirmaActa(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val currentState = _uiState.value

                // Validar que al menos el fiscalizador principal tenga datos completos
                if (currentState.nombreFiscalizadorPrincipal.isBlank() ||
                    currentState.ccFiscalizadorPrincipal.isBlank() ||
                    currentState.cargoFiscalizadorPrincipal.isBlank() ||
                    currentState.firmaFiscalizadorPrincipal == null) {
                    onError("Debe completar todos los datos del fiscalizador principal")
                    _uiState.update { it.copy(isLoading = false) }
                    return@launch
                }

                // Convertir bitmaps a Base64
                val base64Principal = withContext(Dispatchers.IO) { currentState.firmaFiscalizadorPrincipal?.let { bitmapToBase64(it) } }
                val base64Secundario = withContext(Dispatchers.IO) { currentState.firmaFiscalizadorSecundario?.let { bitmapToBase64(it) } }
                val base64Operador = withContext(Dispatchers.IO) { currentState.firmaOperador?.let { bitmapToBase64(it) } }

                val firmaActa = FirmaActaEntity(
                    uuidActa = actaUuid,
                    nombreFiscalizadorPrincipal = currentState.nombreFiscalizadorPrincipal,
                    ccFiscalizadorPrincipal = currentState.ccFiscalizadorPrincipal,
                    cargoFiscalizadorPrincipal = currentState.cargoFiscalizadorPrincipal,
                    firmaFiscalizadorPrincipal = base64Principal,

                    nombreFiscalizadorSecundario = currentState.nombreFiscalizadorSecundario.takeIf { it.isNotBlank() },
                    ccFiscalizadorSecundario = currentState.ccFiscalizadorSecundario.takeIf { it.isNotBlank() },
                    cargoFiscalizadorSecundario = currentState.cargoFiscalizadorSecundario.takeIf { it.isNotBlank() },
                    firmaFiscalizadorSecundario = base64Secundario,

                    nombreOperador = currentState.nombreOperador.takeIf { it.isNotBlank() },
                    ccOperador = currentState.ccOperador.takeIf { it.isNotBlank() },
                    cargoOperador = currentState.cargoOperador.takeIf { it.isNotBlank() },
                    firmaOperador = base64Operador
                )

                withContext(Dispatchers.IO) { firmaActaDao.insertFirmaActa(firmaActa) }

                _uiState.update { it.copy(isLoading = false, isSaved = true) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error al guardar las firmas: ${e.message}"
                    )
                }
                onError("Error al guardar las firmas: ${e.message}")
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }

}