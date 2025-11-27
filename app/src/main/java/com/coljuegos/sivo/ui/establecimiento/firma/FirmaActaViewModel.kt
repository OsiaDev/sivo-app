package com.coljuegos.sivo.ui.establecimiento.firma

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.dao.FirmaActaDao
import com.coljuegos.sivo.data.entity.FirmaActaEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class FirmaActaViewModel @Inject constructor(
    private val firmaActaDao: FirmaActaDao,
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
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                nombreFiscalizadorPrincipal = firmaActa.nombreFiscalizadorPrincipal ?: "",
                                ccFiscalizadorPrincipal = firmaActa.ccFiscalizadorPrincipal ?: "",
                                cargoFiscalizadorPrincipal = firmaActa.cargoFiscalizadorPrincipal ?: "",
                                firmaFiscalizadorPrincipal = firmaActa.firmaFiscalizadorPrincipal?.let { base64ToBitmap(it) },

                                nombreFiscalizadorSecundario = firmaActa.nombreFiscalizadorSecundario ?: "",
                                ccFiscalizadorSecundario = firmaActa.ccFiscalizadorSecundario ?: "",
                                cargoFiscalizadorSecundario = firmaActa.cargoFiscalizadorSecundario ?: "",
                                firmaFiscalizadorSecundario = firmaActa.firmaFiscalizadorSecundario?.let { base64ToBitmap(it) },

                                nombreOperador = firmaActa.nombreOperador ?: "",
                                ccOperador = firmaActa.ccOperador ?: "",
                                cargoOperador = firmaActa.cargoOperador ?: "",
                                firmaOperador = firmaActa.firmaOperador?.let { base64ToBitmap(it) }
                            )
                        }

                        // Actualizar los StateFlows de las firmas
                        _firmaPrincipalBitmap.value = _uiState.value.firmaFiscalizadorPrincipal
                        _firmaSecundarioBitmap.value = _uiState.value.firmaFiscalizadorSecundario
                        _firmaOperadorBitmap.value = _uiState.value.firmaOperador
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
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
                val existingFirmaActa = firmaActaDao.getFirmaActaByActaUuidSuspend(actaUuid)

                val firmaActaToSave = existingFirmaActa?.copy(
                    nombreFiscalizadorPrincipal = currentState.nombreFiscalizadorPrincipal.takeIf { it.isNotBlank() },
                    ccFiscalizadorPrincipal = currentState.ccFiscalizadorPrincipal.takeIf { it.isNotBlank() },
                    cargoFiscalizadorPrincipal = currentState.cargoFiscalizadorPrincipal.takeIf { it.isNotBlank() },
                    firmaFiscalizadorPrincipal = currentState.firmaFiscalizadorPrincipal?.let { bitmapToBase64(it) },

                    nombreFiscalizadorSecundario = currentState.nombreFiscalizadorSecundario.takeIf { it.isNotBlank() },
                    ccFiscalizadorSecundario = currentState.ccFiscalizadorSecundario.takeIf { it.isNotBlank() },
                    cargoFiscalizadorSecundario = currentState.cargoFiscalizadorSecundario.takeIf { it.isNotBlank() },
                    firmaFiscalizadorSecundario = currentState.firmaFiscalizadorSecundario?.let { bitmapToBase64(it) },

                    nombreOperador = currentState.nombreOperador.takeIf { it.isNotBlank() },
                    ccOperador = currentState.ccOperador.takeIf { it.isNotBlank() },
                    cargoOperador = currentState.cargoOperador.takeIf { it.isNotBlank() },
                    firmaOperador = currentState.firmaOperador?.let { bitmapToBase64(it) }
                ) ?: FirmaActaEntity(
                    uuidActa = actaUuid,
                    nombreFiscalizadorPrincipal = currentState.nombreFiscalizadorPrincipal.takeIf { it.isNotBlank() },
                    ccFiscalizadorPrincipal = currentState.ccFiscalizadorPrincipal.takeIf { it.isNotBlank() },
                    cargoFiscalizadorPrincipal = currentState.cargoFiscalizadorPrincipal.takeIf { it.isNotBlank() },
                    firmaFiscalizadorPrincipal = currentState.firmaFiscalizadorPrincipal?.let { bitmapToBase64(it) },

                    nombreFiscalizadorSecundario = currentState.nombreFiscalizadorSecundario.takeIf { it.isNotBlank() },
                    ccFiscalizadorSecundario = currentState.ccFiscalizadorSecundario.takeIf { it.isNotBlank() },
                    cargoFiscalizadorSecundario = currentState.cargoFiscalizadorSecundario.takeIf { it.isNotBlank() },
                    firmaFiscalizadorSecundario = currentState.firmaFiscalizadorSecundario?.let { bitmapToBase64(it) },

                    nombreOperador = currentState.nombreOperador.takeIf { it.isNotBlank() },
                    ccOperador = currentState.ccOperador.takeIf { it.isNotBlank() },
                    cargoOperador = currentState.cargoOperador.takeIf { it.isNotBlank() },
                    firmaOperador = currentState.firmaOperador?.let { bitmapToBase64(it) }
                )

                firmaActaDao.insertFirmaActa(firmaActaToSave)
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
                val firmaActa = FirmaActaEntity(
                    uuidActa = actaUuid,
                    nombreFiscalizadorPrincipal = currentState.nombreFiscalizadorPrincipal,
                    ccFiscalizadorPrincipal = currentState.ccFiscalizadorPrincipal,
                    cargoFiscalizadorPrincipal = currentState.cargoFiscalizadorPrincipal,
                    firmaFiscalizadorPrincipal = currentState.firmaFiscalizadorPrincipal?.let { bitmapToBase64(it) },

                    nombreFiscalizadorSecundario = currentState.nombreFiscalizadorSecundario.takeIf { it.isNotBlank() },
                    ccFiscalizadorSecundario = currentState.ccFiscalizadorSecundario.takeIf { it.isNotBlank() },
                    cargoFiscalizadorSecundario = currentState.cargoFiscalizadorSecundario.takeIf { it.isNotBlank() },
                    firmaFiscalizadorSecundario = currentState.firmaFiscalizadorSecundario?.let { bitmapToBase64(it) },

                    nombreOperador = currentState.nombreOperador.takeIf { it.isNotBlank() },
                    ccOperador = currentState.ccOperador.takeIf { it.isNotBlank() },
                    cargoOperador = currentState.cargoOperador.takeIf { it.isNotBlank() },
                    firmaOperador = currentState.firmaOperador?.let { bitmapToBase64(it) }
                )

                firmaActaDao.insertFirmaActa(firmaActa)

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