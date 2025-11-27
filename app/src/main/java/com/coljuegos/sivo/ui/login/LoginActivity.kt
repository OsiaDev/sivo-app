package com.coljuegos.sivo.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.coljuegos.sivo.R
import com.coljuegos.sivo.databinding.ActivityLoginBinding
import com.coljuegos.sivo.ui.main.MainActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Verificar si ya hay una sesión activa al iniciar
        this.checkExistingSession()

        this.setupListeners()
        this.observeViewModel()
    }

    private fun checkExistingSession() {
        lifecycleScope.launch {
            if (viewModel.isAlreadyLoggedIn()) {
                // Si ya está logueado, ir directamente a MainActivity
                navigateToMain()
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.loginState.collect { state ->
                when (state) {
                    is LoginState.Idle -> {
                        hideLoadingOverlay()
                    }

                    is LoginState.Loading -> {
                        showLoadingOverlay()
                    }

                    is LoginState.Success -> {
                        hideLoadingOverlay()
                        showSuccessMessage(getString(R.string.login_success, state.loginResponse.user.nameUser))
                        navigateToMain()
                    }

                    is LoginState.Error -> {
                        hideLoadingOverlay()
                        showErrorMessage(state.message)
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        // Listener para el checkbox de mostrar contraseña
        binding.showPassword.setOnCheckedChangeListener { _, isChecked ->
            this.togglePasswordVisibility(isChecked)
        }

        // Listener para el botón de login
        binding.loginButton.setOnClickListener {
            if (this.validateFields()) {
                this.login()
            }
        }
    }

    private fun togglePasswordVisibility(isVisible: Boolean) {
        if (isVisible) {
            // Mostrar contraseña
            binding.incomePassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            // Ocultar contraseña
            binding.incomePassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        // Mantener el cursor al final del texto
        binding.incomePassword.text?.length?.let { length ->
            binding.incomePassword.setSelection(length)
        }
    }

    private fun validateFields() : Boolean {
        var isValid = true
        val username = binding.incomeUsername.text.toString().trim()
        val password = binding.incomePassword.text.toString().trim()
        if (username.isEmpty()) {
            binding.layoutIncomeUsername.error = "El nombre de usuario es requerido"
            binding.incomeUsername.requestFocus()
            isValid = false
        } else {
            binding.layoutIncomeUsername.error = null
        }
        if (password.isEmpty()) {
            binding.layoutIncomePassword.error = "La contraseña es requerida"
            binding.incomePassword.requestFocus()
            isValid = false
        } else {
            binding.layoutIncomePassword.error = null
        }
        return isValid
    }

    private fun login() {
        val username = binding.incomeUsername.text.toString().trim()
        val password = binding.incomePassword.text.toString().trim()

        // Limpiar errores previos
        binding.layoutIncomeUsername.error = null
        binding.layoutIncomePassword.error = null

        this.showLoadingOverlay()
        // Realizar login
        viewModel.login(username, password)
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showLoadingOverlay() {
        binding.loadingOverlay.root.visibility = View.VISIBLE
        binding.mainLayout.isEnabled = false
        binding.loginButton.isEnabled = false
    }

    private fun hideLoadingOverlay() {
        binding.loadingOverlay.root.visibility = View.GONE
        // Enable interaction with main content
        binding.mainLayout.isEnabled = true
        binding.bottomNavigation.isEnabled = true
        binding.loginButton.isEnabled = true
    }

    private fun showSuccessMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showErrorMessage(message: String) {
        val messageToShow = if (message.contains(getString(R.string.login_error_connection_compare), ignoreCase = true)) {
            getString(R.string.login_error_connection)
        } else {
            message
        }

        binding.root.post {
            Snackbar.make(binding.mainLayout, messageToShow, Snackbar.LENGTH_LONG)
                .setAnchorView(binding.bottomNavigation)
                .show()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        viewModel.resetLoginState()
    }

}