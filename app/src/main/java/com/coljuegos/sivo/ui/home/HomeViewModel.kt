package com.coljuegos.sivo.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.repository.ActasRepository
import com.coljuegos.sivo.utils.NetworkResult
import com.coljuegos.sivo.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val actasRepository: ActasRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())

    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
        loadActas()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            try {
                val currentSession = sessionManager.getCurrentSession()
                val fullName = currentSession?.fullNameUserSession

                _uiState.value = _uiState.value.copy(
                    userFullName = fullName
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al cargar datos del usuario: ${e.message}"
                )
            }
        }
    }

    fun loadActas() {
        viewModelScope.launch {
            actasRepository.getActasByCurrentUser().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                    }

                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            actas = result.data ?: emptyList(),
                            errorMessage = null,
                            isRefreshing = false
                        )
                    }

                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message,
                            isRefreshing = false
                        )
                    }
                }
            }
        }
    }

    /**
     * Método llamado cuando el usuario desliza hacia abajo (pull-to-refresh)
     * Fuerza la recarga de actas desde el backend y actualiza la base de datos local
     */
    fun refreshActasFromBackend() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, errorMessage = null)

            actasRepository.refreshActasFromBackend().collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        // Mantener isRefreshing = true
                    }

                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isRefreshing = false,
                            actas = result.data ?: emptyList(),
                            successMessage = "Actas actualizadas correctamente",
                            errorMessage = null
                        )
                    }

                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isRefreshing = false,
                            errorMessage = result.message ?: "Error al actualizar actas"
                        )
                    }
                }
            }
        }
    }

    fun refreshActas() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadActas()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

}