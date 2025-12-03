package com.coljuegos.sivo.ui.establecimiento.verificacion

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ScrollView
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
            if (validateRequiredFields()) {
                val currentState = viewModel.uiState.value
                currentState.actaUuid?.let { acta ->
                    val action = VerificacionContractualFragmentDirections
                        .actionVerificacionContractualFragmentToVerificacionSiplaftFragment(acta)
                    findNavController().navigate(action)
                }
            }

        }
    }

    private fun validateRequiredFields(): Boolean {
        val currentState = viewModel.uiState.value

        // Limpiar errores previos
        binding.layoutPregunta1.error = null
        binding.layoutPregunta2.error = null
        binding.otraDireccionLayout.error = null
        binding.layoutPregunta3.error = null
        binding.otroNombreLayout.error = null
        binding.layoutPregunta4.error = null
        binding.layoutTipoActividad.error = null
        binding.layoutOtrosActividad.error = null
        binding.layoutPregunta5.error = null

        var isValid = true
        var firstErrorField: View? = null

        // Validar campo obligatorio: Aviso de autorización
        if (currentState.avisoAutorizacion.isBlank()) {
            binding.layoutPregunta1.error = getString(R.string.verificacion_contractual_validacion_aviso)
            isValid = false
            if (firstErrorField == null) firstErrorField = binding.layoutPregunta1
        }

        // Validar campo obligatorio: Dirección corresponde
        if (currentState.direccionCorresponde.isBlank()) {
            binding.layoutPregunta2.error = getString(R.string.verificacion_contractual_validacion_direccion)
            isValid = false
            if (firstErrorField == null) firstErrorField = binding.layoutPregunta2
        }

        // Si la dirección NO corresponde, validar que se haya ingresado la dirección correcta
        if (currentState.mostrarCampoOtraDireccion && currentState.otraDireccion.isBlank()) {
            binding.otraDireccionLayout.error = getString(R.string.verificacion_contractual_validacion_otra_direccion)
            isValid = false
            if (firstErrorField == null) firstErrorField = binding.otraDireccionLayout
        }

        // Validar campo obligatorio: Nombre establecimiento corresponde
        if (currentState.nombreEstablecimientoCorresponde.isBlank()) {
            binding.layoutPregunta3.error = getString(R.string.verificacion_contractual_validacion_nombre)
            isValid = false
            if (firstErrorField == null) firstErrorField = binding.layoutPregunta3
        }

        // Si el nombre NO corresponde, validar que se haya ingresado el nombre correcto
        if (currentState.mostrarCampoOtroNombre && currentState.otroNombre.isBlank()) {
            binding.otroNombreLayout.error = getString(R.string.verificacion_contractual_validacion_otro_nombre)
            isValid = false
            if (firstErrorField == null) firstErrorField = binding.otroNombreLayout
        }

        // Validar campo obligatorio: Desarrolla actividades diferentes
        if (currentState.desarrollaActividadesDiferentes.isBlank()) {
            binding.layoutPregunta4.error = getString(R.string.verificacion_contractual_validacion_actividades)
            isValid = false
            if (firstErrorField == null) firstErrorField = binding.layoutPregunta4
        }

        // Si desarrolla actividades diferentes, validar campos relacionados
        if (currentState.mostrarSeccionActividadesDiferentes) {
            // Debe seleccionar el tipo de actividad
            if (currentState.tipoActividad.isBlank()) {
                binding.layoutTipoActividad.error = getString(R.string.verificacion_contractual_validacion_tipo_actividad)
                isValid = false
                if (firstErrorField == null) firstErrorField = binding.layoutTipoActividad
            }

            // Si es "Otros", debe especificar
            if (currentState.mostrarCampoOtros && currentState.especificacionOtros.isBlank()) {
                binding.layoutOtrosActividad.error = getString(R.string.verificacion_contractual_validacion_especificacion)
                isValid = false
                if (firstErrorField == null) firstErrorField = binding.layoutOtrosActividad
            }
        }

        // Validar campo obligatorio: Cuenta con registros de mantenimiento
        if (currentState.cuentaRegistrosMantenimiento.isBlank()) {
            binding.layoutPregunta5.error = getString(R.string.verificacion_contractual_validacion_registros)
            isValid = false
            if (firstErrorField == null) firstErrorField = binding.layoutPregunta5
        }

        // Si hay errores, mostrar mensaje y hacer scroll al primer campo con error
        if (!isValid) {
            Snackbar.make(
                binding.root,
                getString(R.string.verificacion_contractual_validacion_campos_incompletos),
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