package com.coljuegos.sivo.ui.login

import com.coljuegos.sivo.data.remote.model.LoginResponseDTO

sealed class LoginState {

    object Idle : LoginState()

    object Loading : LoginState()

    data class Success(val loginResponse: LoginResponseDTO) : LoginState()

    data class Error(val message: String) : LoginState()

}