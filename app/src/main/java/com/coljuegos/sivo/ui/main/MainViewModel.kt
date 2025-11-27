package com.coljuegos.sivo.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _sessionState = MutableStateFlow(true)

    val sessionState: StateFlow<Boolean> = _sessionState.asStateFlow()

    private val _userName = MutableStateFlow<String?>(null)

    val userName: StateFlow<String?> = _userName.asStateFlow()

    init {
        // Observar cambios en la sesión
        observeSession()
        loadUserData()
    }

    private fun observeSession() {
        viewModelScope.launch {
            sessionManager.getCurrentSessionFlow().collect { session ->
                _sessionState.value = session != null
                _userName.value = session?.nameUserSession
            }
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            try {
                val userName = sessionManager.getUserName()
                _userName.value = userName
            } catch (_: Exception) {
                _sessionState.value = false
            }
        }
    }

    /**
     * Verifica si la sesión actual es válida
     */
    suspend fun isSessionValid(): Boolean {
        return try {
            sessionManager.isLoggedIn()
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Cierra la sesión del usuario
     */
    suspend fun logout() {
        try {
            sessionManager.logout()
            _sessionState.value = false
            _userName.value = null
        } catch (_: Exception) {
            // En caso de error, asumir que se debe cerrar sesión
            _sessionState.value = false
        }
    }

}