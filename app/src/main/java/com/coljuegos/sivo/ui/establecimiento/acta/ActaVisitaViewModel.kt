package com.coljuegos.sivo.ui.establecimiento.acta

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.dao.ActaVisitaDao
import com.coljuegos.sivo.data.dao.MunicipioDao
import com.coljuegos.sivo.data.entity.ActaVisitaEntity
import com.coljuegos.sivo.data.entity.MunicipioDisplayItem
import com.coljuegos.sivo.data.repository.ActasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ActaVisitaViewModel @Inject constructor(
    private val actasRepository: ActasRepository,
    private val municipioDao: MunicipioDao,
    private val actaVisitaDao: ActaVisitaDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val actaUuid: UUID = checkNotNull(savedStateHandle.get<UUID>("actaUuid"))

    private val _uiState = MutableStateFlow(ActaVisitaUiState())

    val uiState: StateFlow<ActaVisitaUiState> = _uiState.asStateFlow()

    init {
        loadActaDetails()
        loadMunicipios()
        loadActaVisita()
    }

    private fun loadActaDetails() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                val acta = actasRepository.getActaByUuid(actaUuid)
                if (acta != null) {
                    val funcionarios = actasRepository.getFuncionariosByActa(actaUuid)
                    val inventarios = actasRepository.getInventariosByActa(actaUuid)

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        acta = acta,
                        funcionarios = funcionarios,
                        inventarios = inventarios,
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "No se encontró el acta especificada"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar el acta: ${e.message}"
                )
            }
        }
    }

    private fun loadMunicipios() {
        viewModelScope.launch {
            try {
                val municipios = municipioDao.getAllMunicipiosWithDepartamento()
                _uiState.value = _uiState.value.copy(municipios = municipios)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al cargar municipios: ${e.message}"
                )
            }
        }
    }

    private fun loadActaVisita() {
        viewModelScope.launch {
            try {
                val existingActaVisita = actaVisitaDao.getActaVisitaByActaId(actaUuid)
                if (existingActaVisita != null) {
                    val selectedMunicipio = if (existingActaVisita.uuidMunicipio != null) {
                        municipioDao.getAllMunicipiosWithDepartamento()
                            .find { it.municipioId == existingActaVisita.uuidMunicipio.toString() }
                    } else null

                    // NUEVO: Convertir String -> Lista
                    val correosList = existingActaVisita.correosContacto
                        ?.split(";")
                        ?.map { it.trim() }
                        ?.filter { it.isNotBlank() }
                        ?: emptyList()

                    _uiState.value = _uiState.value.copy(
                        nombrePresente = existingActaVisita.nombrePresente ?: "",
                        cedulaPresente = existingActaVisita.identificacionPresente ?: "",
                        cargoPresente = existingActaVisita.cargoPresente ?: "",
                        emailPresente = existingActaVisita.emailPresente ?: "",
                        correosContacto = correosList,  // NUEVO
                        selectedMunicipio = selectedMunicipio
                    )
                }
            } catch (_: Exception) {
                // Error silencioso, continúa con valores por defecto
            }
        }
    }

    private fun saveActaVisita() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value

                // NUEVO: Convertir Lista -> String
                val correosString = currentState.correosContacto
                    .filter { it.isNotBlank() }
                    .joinToString(";")
                    .takeIf { it.isNotEmpty() }

                // Buscar si ya existe un registro
                val existingActaVisita = actaVisitaDao.getActaVisitaByActaId(actaUuid)

                val actaVisitaToSave = existingActaVisita?.// Actualizar el existente
                copy(
                    nombrePresente = currentState.nombrePresente.takeIf { it.isNotBlank() },
                    identificacionPresente = currentState.cedulaPresente.takeIf { it.isNotBlank() },
                    uuidMunicipio = currentState.selectedMunicipio?.let { UUID.fromString(it.municipioId) },
                    cargoPresente = currentState.cargoPresente.takeIf { it.isNotBlank() },
                    emailPresente = currentState.emailPresente.takeIf { it.isNotBlank() },
                    correosContacto = correosString  // NUEVO
                )
                    ?: // Crear nuevo
                    ActaVisitaEntity(
                        uuidActa = actaUuid,
                        nombrePresente = currentState.nombrePresente.takeIf { it.isNotBlank() },
                        identificacionPresente = currentState.cedulaPresente.takeIf { it.isNotBlank() },
                        uuidMunicipio = currentState.selectedMunicipio?.let { UUID.fromString(it.municipioId) },
                        cargoPresente = currentState.cargoPresente.takeIf { it.isNotBlank() },
                        emailPresente = currentState.emailPresente.takeIf { it.isNotBlank() },
                        correosContacto = correosString  // NUEVO
                    )

                actaVisitaDao.insert(actaVisitaToSave)
            } catch (_: Exception) {
                // Error silencioso para no interrumpir la experiencia del usuario
            }
        }
    }

    fun selectMunicipio(municipio: MunicipioDisplayItem) {
        _uiState.value = _uiState.value.copy(selectedMunicipio = municipio)
        saveActaVisita()
    }

    fun updateNombrePresente(nombre: String) {
        _uiState.value = _uiState.value.copy(nombrePresente = nombre)
        saveActaVisita()
    }

    fun updateCedulaPresente(cedula: String) {
        _uiState.value = _uiState.value.copy(cedulaPresente = cedula)
        saveActaVisita()
    }

    fun updateCargoPresente(cargo: String) {
        _uiState.value = _uiState.value.copy(cargoPresente = cargo)
        saveActaVisita()
    }

    fun updateEmailPresente(email: String) {
        _uiState.value = _uiState.value.copy(emailPresente = email)
        saveActaVisita()
    }

    // NUEVOS MÉTODOS PARA MANEJAR CORREOS
    fun addCorreoContacto(correo: String) {
        val correoTrimmed = correo.trim()
        if (correoTrimmed.isNotEmpty() && !_uiState.value.correosContacto.contains(correoTrimmed)) {
            _uiState.value = _uiState.value.copy(
                correosContacto = _uiState.value.correosContacto + correoTrimmed
            )
            saveActaVisita()
        }
    }

    fun removeCorreoContacto(correo: String) {
        _uiState.value = _uiState.value.copy(
            correosContacto = _uiState.value.correosContacto - correo
        )
        saveActaVisita()
    }

    fun retry() {
        loadActaDetails()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

}