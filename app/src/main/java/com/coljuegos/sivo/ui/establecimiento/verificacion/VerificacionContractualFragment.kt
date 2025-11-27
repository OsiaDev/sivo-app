package com.coljuegos.sivo.ui.establecimiento.verificacion

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.coljuegos.sivo.R
import com.coljuegos.sivo.databinding.FragmentVerificacionContractualBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VerificacionContractualFragment : Fragment() {

    private var _binding: FragmentVerificacionContractualBinding? = null

    private val binding get() = _binding!!

    private val viewModel: VerificacionContractualViewModel by viewModels()

    // Adapters para los spinners
    private lateinit var adapterSiNoNa: ArrayAdapter<String>

    private lateinit var adapterTipoActividad: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("ActaVisitaFragment", "Registrando listener")

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerificacionContractualBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        setupClickListeners()
        setupInputBindings()
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

    override fun onPause() {
        super.onPause()
        // Limpiar listener cuando el fragment no es visible
        parentFragmentManager.clearFragmentResultListener("camera_action")
    }

    private fun setupAdapters() {
        // Adapter para opciones Si/No/N/A
        val opcionesSiNoNa = resources.getStringArray(R.array.si_no_na_options)
        adapterSiNoNa = ArrayAdapter(
            requireContext(),
            R.layout.item_dropdown,
            opcionesSiNoNa
        )

        // Adapter para tipos de actividad
        val opcionesActividades = resources.getStringArray(R.array.actividades_options)
        adapterTipoActividad = ArrayAdapter(
            requireContext(),
            R.layout.item_dropdown,
            opcionesActividades
        )

        // Asignar adapters a los spinners
        binding.pregunta1Spinner.setAdapter(adapterSiNoNa)
        binding.pregunta2Spinner.setAdapter(adapterSiNoNa)
        binding.pregunta3Spinner.setAdapter(adapterSiNoNa)
        binding.pregunta4Spinner.setAdapter(adapterSiNoNa)
        binding.pregunta5Spinner.setAdapter(adapterSiNoNa)
        binding.tipoActividadSpinner.setAdapter(adapterTipoActividad)
    }

    private fun navigateToGallery() {
        val currentState = viewModel.uiState.value
        currentState.actaUuid?.let { acta ->
            val action = VerificacionContractualFragmentDirections.actionVerificacionContractualFragmentToGalleryFragment(acta, "verificacion_contractual")
            findNavController().navigate(action)
        }
    }

    private fun setupClickListeners() {
        // Spinners
        binding.pregunta1Spinner.setOnItemClickListener { _, _, position, _ ->
            val selectedValue = adapterSiNoNa.getItem(position) ?: ""
            viewModel.updateAvisoAutorizacion(selectedValue)
        }

        binding.pregunta2Spinner.setOnItemClickListener { _, _, position, _ ->
            val selectedValue = adapterSiNoNa.getItem(position) ?: ""
            viewModel.updateDireccionCorresponde(selectedValue)
        }

        binding.pregunta3Spinner.setOnItemClickListener { _, _, position, _ ->
            val selectedValue = adapterSiNoNa.getItem(position) ?: ""
            viewModel.updateNombreEstablecimientoCorresponde(selectedValue)
        }

        binding.pregunta4Spinner.setOnItemClickListener { _, _, position, _ ->
            val selectedValue = adapterSiNoNa.getItem(position) ?: ""
            viewModel.updateDesarrollaActividadesDiferentes(selectedValue)
        }

        binding.pregunta5Spinner.setOnItemClickListener { _, _, position, _ ->
            val selectedValue = adapterSiNoNa.getItem(position) ?: ""
            viewModel.updateCuentaRegistrosMantenimiento(selectedValue)
        }

        binding.tipoActividadSpinner.setOnItemClickListener { _, _, position, _ ->
            val selectedValue = adapterTipoActividad.getItem(position) ?: ""
            viewModel.updateTipoActividad(selectedValue)
        }

        // Botones de navegación
        binding.btnAnterior.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSiguiente.setOnClickListener {
            // Navegaría al siguiente fragment (Verificación cumplimiento siplaft)
            val currentState = viewModel.uiState.value
            currentState.actaUuid?.let { acta ->
                val action = VerificacionContractualFragmentDirections
                    .actionVerificacionContractualFragmentToVerificacionSiplaftFragment(acta)
                findNavController().navigate(action)
            }
        }
    }

    private fun setupInputBindings() {
        binding.otraDireccionText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateOtraDireccion(text?.toString() ?: "")
        }

        binding.otroNombreText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateOtroNombre(text?.toString() ?: "")
        }

        binding.otrosActividadText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateEspecificacionOtros(text?.toString() ?: "")
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                updateUI(uiState)
            }
        }
    }

    private fun updateUI(uiState: VerificacionContractualUiState) {
        // Mostrar/ocultar loading
        if (uiState.isLoading) {
            showLoading()
        } else {
            hideLoading()
        }

        // Mostrar errores
        uiState.errorMessage?.let { errorMessage ->
            showError(errorMessage)
            viewModel.clearError()
        }

        // Actualizar valores de los spinners
        updateSpinnerValue(binding.pregunta1Spinner, uiState.avisoAutorizacion)
        updateSpinnerValue(binding.pregunta2Spinner, uiState.direccionCorresponde)
        updateSpinnerValue(binding.pregunta3Spinner, uiState.nombreEstablecimientoCorresponde)
        updateSpinnerValue(binding.pregunta4Spinner, uiState.desarrollaActividadesDiferentes)
        updateSpinnerValue(binding.pregunta5Spinner, uiState.cuentaRegistrosMantenimiento)
        updateSpinnerValue(binding.tipoActividadSpinner, uiState.tipoActividad)

        if (binding.otraDireccionText.text.toString() != uiState.otraDireccion) {
            binding.otraDireccionText.setText(uiState.otraDireccion)
        }

        if (binding.otroNombreText.text.toString() != uiState.otroNombre) {
            binding.otroNombreText.setText(uiState.otroNombre)
        }

        // Actualizar campo de texto para "Otros"
        if (binding.otrosActividadText.text.toString() != uiState.especificacionOtros) {
            binding.otrosActividadText.setText(uiState.especificacionOtros)
        }

        binding.otraDireccionLayout.visibility =
            if (uiState.mostrarCampoOtraDireccion) View.VISIBLE else View.GONE

        binding.otroNombreLayout.visibility =
            if (uiState.mostrarCampoOtroNombre) View.VISIBLE else View.GONE

        // Mostrar/ocultar secciones condicionales
        binding.layoutActividadesDiferentes.visibility =
            if (uiState.mostrarSeccionActividadesDiferentes) View.VISIBLE else View.GONE

        binding.layoutOtrosActividad.visibility =
            if (uiState.mostrarCampoOtros) View.VISIBLE else View.GONE
    }

    private fun updateSpinnerValue(spinner: android.widget.AutoCompleteTextView, value: String) {
        if (spinner.text.toString() != value && value.isNotEmpty()) {
            spinner.setText(value, false)
        }
    }

    private fun showLoading() {
        binding.constraintLayout.alpha = 0.3f
        binding.constraintLayout.isEnabled = false
    }

    private fun hideLoading() {
        binding.constraintLayout.alpha = 1.0f
        binding.constraintLayout.isEnabled = true
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}