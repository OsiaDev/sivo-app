package com.coljuegos.sivo.ui.establecimiento.siplaft

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ScrollView
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.coljuegos.sivo.R
import com.coljuegos.sivo.databinding.FragmentVerificacionSiplaftBinding
import com.coljuegos.sivo.ui.establecimiento.verificacion.VerificacionContractualFragmentDirections
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VerificacionSiplaftFragment : Fragment() {

    private var _binding: FragmentVerificacionSiplaftBinding? = null

    private val binding get() = _binding!!

    private val viewModel: VerificacionSiplaftViewModel by viewModels()

    // Adapters para los spinners
    private lateinit var adapterSiNoNa: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("VerificacionSiplaftFragment", "Registrando listener")

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerificacionSiplaftBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        setupSpinners()
        setupInputBindings()
        setupButtons()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        // Registrar listener cuando el fragment es visible
        parentFragmentManager.setFragmentResultListener("camera_action", viewLifecycleOwner) { _, _ ->
            Log.d("ActaVisitaFragment", "Recibido evento de cámara")
            navigateToGallery()
        }
    }

    private fun setupAdapters() {
        // Adapter para Si/No/NA
        val opcionesSiNoNa = resources.getStringArray(R.array.si_no_na_options)
        adapterSiNoNa = ArrayAdapter(
            requireContext(),
            R.layout.item_dropdown,
            opcionesSiNoNa
        )
    }

    private fun setupSpinners() {
        // Configurar adaptadores para los AutoCompleteTextView
        binding.pregunta1Spinner.setAdapter(adapterSiNoNa)
        binding.pregunta2Spinner.setAdapter(adapterSiNoNa)
        binding.pregunta3Spinner.setAdapter(adapterSiNoNa)

        // Listeners para actualizar el ViewModel
        binding.pregunta1Spinner.setOnItemClickListener { _, _, position, _ ->
            val selectedValue = adapterSiNoNa.getItem(position) ?: ""
            viewModel.updateCuentaFormatoIdentificacion(selectedValue)
        }

        binding.pregunta2Spinner.setOnItemClickListener { _, _, position, _ ->
            val selectedValue = adapterSiNoNa.getItem(position) ?: ""
            viewModel.updateCuentaFormatoReporteInterno(selectedValue)
        }

        binding.pregunta3Spinner.setOnItemClickListener { _, _, position, _ ->
            val selectedValue = adapterSiNoNa.getItem(position) ?: ""
            viewModel.updateConoceCodigoConducta(selectedValue)
        }
    }

    private fun setupInputBindings() {
        binding.montoText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateMontoIdentificacion(text?.toString() ?: "")
        }

        binding.senalesAlertaText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateSenalesAlerta(text?.toString() ?: "")
        }
    }

    private fun setupButtons() {
        // Botones de navegación
        binding.btnAnterior.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSiguiente.setOnClickListener {
            if (validateRequiredFields()) {
                val currentState = viewModel.uiState.value
                currentState.actaUuid?.let { acta ->
                    val action = VerificacionSiplaftFragmentDirections
                        .actionVerificacionSiplaftFragmentToInventarioFragment(acta)
                    findNavController().navigate(action)
                }
            }
        }
    }

    private fun validateRequiredFields(): Boolean {
        val currentState = viewModel.uiState.value

        // Limpiar errores previos
        binding.pregunta1Layout.error = null
        binding.montoLayout.error = null
        binding.pregunta2Layout.error = null
        binding.senalesAlertaLayout.error = null
        binding.pregunta3Layout.error = null

        var isValid = true
        var firstErrorField: View? = null

        // Validar pregunta 1: Cuenta con formato de identificación
        if (currentState.cuentaFormatoIdentificacion.isBlank()) {
            binding.pregunta1Layout.error = getString(R.string.verificacion_siplaft_validacion_pregunta1)
            isValid = false
            if (firstErrorField == null) firstErrorField = binding.pregunta1Layout
        }

        // Si muestra el campo de monto, validar que esté lleno
        if (currentState.mostrarCampoMonto && currentState.montoIdentificacion.isBlank()) {
            binding.montoLayout.error = getString(R.string.verificacion_siplaft_validacion_monto)
            isValid = false
            if (firstErrorField == null) firstErrorField = binding.montoLayout
        }

        // Validar pregunta 2: Cuenta con formato de reporte interno
        if (currentState.cuentaFormatoReporteInterno.isBlank()) {
            binding.pregunta2Layout.error = getString(R.string.verificacion_siplaft_validacion_pregunta2)
            isValid = false
            if (firstErrorField == null) firstErrorField = binding.pregunta2Layout
        }

        // Si muestra el campo de señales de alerta, validar que esté lleno
        if (currentState.mostrarCampoSenales && currentState.senalesAlerta.isBlank()) {
            binding.senalesAlertaLayout.error = getString(R.string.verificacion_siplaft_validacion_senales)
            isValid = false
            if (firstErrorField == null) firstErrorField = binding.senalesAlertaLayout
        }

        // Validar pregunta 3: Conoce código de conducta
        if (currentState.conoceCodigoConducta.isBlank()) {
            binding.pregunta3Layout.error = getString(R.string.verificacion_siplaft_validacion_pregunta3)
            isValid = false
            if (firstErrorField == null) firstErrorField = binding.pregunta3Layout
        }

        // Si hay errores, mostrar mensaje y hacer scroll al primer campo con error
        if (!isValid) {
            Snackbar.make(
                binding.root,
                getString(R.string.verificacion_siplaft_validacion_campos_incompletos),
                Snackbar.LENGTH_LONG
            ).show()

            // Hacer scroll al primer campo con error
            firstErrorField?.let { field ->
                binding.root.post {
                    val scrollView = binding.root.parent as? ScrollView
                    scrollView?.smoothScrollTo(0, field.top)
                    field.requestFocus()
                }
            }
        }

        return isValid
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                updateUI(uiState)
            }
        }
    }

    private fun updateUI(uiState: VerificacionSiplaftUiState) {
        // Mostrar errores
        uiState.errorMessage?.let { errorMessage ->
            showError(errorMessage)
            viewModel.clearError()
        }

        // Actualizar visibilidad de campos condicionales
        binding.montoLayout.isVisible = uiState.mostrarCampoMonto
        binding.senalesAlertaLayout.isVisible = uiState.mostrarCampoSenales

        // Restaurar datos del formulario guardados
        restoreFormData(uiState)
    }

    private fun restoreFormData(uiState: VerificacionSiplaftUiState) {
        // Restaurar valores de los spinners
        if (uiState.cuentaFormatoIdentificacion.isNotBlank()) {
            binding.pregunta1Spinner.setText(uiState.cuentaFormatoIdentificacion, false)
        }

        if (uiState.cuentaFormatoReporteInterno.isNotBlank()) {
            binding.pregunta2Spinner.setText(uiState.cuentaFormatoReporteInterno, false)
        }

        if (uiState.conoceCodigoConducta.isNotBlank()) {
            binding.pregunta3Spinner.setText(uiState.conoceCodigoConducta, false)
        }

        // Restaurar valores de los campos de texto
        if (uiState.montoIdentificacion.isNotBlank() &&
            binding.montoText.text.toString() != uiState.montoIdentificacion) {
            binding.montoText.setText(uiState.montoIdentificacion)
        }

        if (uiState.senalesAlerta.isNotBlank() &&
            binding.senalesAlertaText.text.toString() != uiState.senalesAlerta) {
            binding.senalesAlertaText.setText(uiState.senalesAlerta)
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Reintentar") {
                viewModel.retry()
            }
            .show()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun navigateToGallery() {
        val currentState = viewModel.uiState.value
        currentState.actaUuid?.let { acta ->
            val action = VerificacionSiplaftFragmentDirections.actionVerificacionSiplaftFragmentToGalleryFragment(acta)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}