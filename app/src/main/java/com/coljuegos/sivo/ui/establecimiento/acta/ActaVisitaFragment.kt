package com.coljuegos.sivo.ui.establecimiento.acta

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.coljuegos.sivo.R
import com.coljuegos.sivo.data.entity.ActaEntity
import com.coljuegos.sivo.databinding.FragmentActaVisitaBinding
import com.coljuegos.sivo.di.Extenxion.orNA
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@AndroidEntryPoint
class ActaVisitaFragment : Fragment() {

    private var _binding: FragmentActaVisitaBinding? = null

    private val binding get() = _binding!!

    private val viewModel: ActaVisitaViewModel by viewModels()

    private lateinit var municipioAdapter: MunicipioAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("ActaVisitaFragment", "Registrando listener")

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActaVisitaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMunicipioSelector()
        setupInputBindings()
        observeViewModel()
        setupExpandableText()
        navigateVerificacion()
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

    private fun setupExpandableText() {
        var expanded = false
        binding.textoLegal1Title.setOnClickListener {
            expanded = !expanded
            binding.textoLegal1Title.maxLines = if (expanded) Int.MAX_VALUE else 2
        }
    }

    private fun navigateVerificacion() {
        binding.btnEnviarMaterial.setOnClickListener {
            val currentState = viewModel.uiState.value
            currentState.acta?.let { acta ->
                val action = ActaVisitaFragmentDirections.actionActaVisitaFragmentToVerificacionContractualFragment(acta.uuidActa)
                findNavController().navigate(action)
            }
        }
    }

    private fun navigateToGallery() {
        val currentState = viewModel.uiState.value
        currentState.acta?.let { acta ->
            val action = ActaVisitaFragmentDirections.actionActaVisitaFragmentToGalleryFragment(acta.uuidActa, "acta_visita")
            findNavController().navigate(action)
        }
    }

    private fun setupMunicipioSelector() {
        municipioAdapter = MunicipioAdapter(requireContext(), emptyList())
        binding.municipioExpedicion.setAdapter(municipioAdapter)

        binding.municipioExpedicion.setOnItemClickListener { _, _, position, _ ->
            val selectedMunicipio = municipioAdapter.getItem(position)
            viewModel.selectMunicipio(selectedMunicipio)
        }
    }

    private fun setupInputBindings() {
        binding.nombrePresente.doOnTextChanged { text, _, _, _ ->
            viewModel.updateNombrePresente(text?.toString() ?: "")
        }

        binding.cedulaPresente.doOnTextChanged { text, _, _, _ ->
            viewModel.updateCedulaPresente(text?.toString() ?: "")
        }

        binding.calidad.doOnTextChanged { text, _, _, _ ->
            viewModel.updateCargoPresente(text?.toString() ?: "")
        }

        binding.email.doOnTextChanged { text, _, _, _ ->
            viewModel.updateEmailPresente(text?.toString() ?: "")
        }

        // NUEVO: Listener para agregar correos
        binding.btnAgregarCorreo.setOnClickListener {
            agregarCorreo()
        }

        binding.nuevoCorreoInput.setOnEditorActionListener { _, _, _ ->
            agregarCorreo()
            true
        }

        binding.btnCapturarIdentificacion.setOnClickListener {
            val currentState = viewModel.uiState.value
            currentState.acta?.let { acta ->
                val action = ActaVisitaFragmentDirections.actionActaVisitaFragmentToGalleryFragment(acta.uuidActa, "foto_identificacion")
                findNavController().navigate(action)
            }
        }
    }

    private fun agregarCorreo() {
        val correo = binding.nuevoCorreoInput.text.toString().trim()

        // Limpiar error previo
        binding.layoutNuevoCorreo.error = null

        when {
            correo.isEmpty() -> {
                binding.layoutNuevoCorreo.error = getString(R.string.acta_visita_correo_vacio)
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches() -> {
                binding.layoutNuevoCorreo.error = getString(R.string.acta_visita_correo_invalido)
            }
            viewModel.uiState.value.correosContacto.contains(correo) -> {
                binding.layoutNuevoCorreo.error = getString(R.string.acta_visita_correo_duplicado)
            }
            else -> {
                viewModel.addCorreoContacto(correo)
                binding.nuevoCorreoInput.text?.clear()
                // Ocultar el teclado
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(binding.nuevoCorreoInput.windowToken, 0)
            }
        }
    }

    private fun updateCorreosChips(correos: List<String>) {
        binding.chipGroupCorreos.removeAllViews()

        correos.forEach { correo ->
            val chip = Chip(requireContext()).apply {
                text = correo
                isCloseIconVisible = true
                setOnCloseIconClickListener {
                    viewModel.removeCorreoContacto(correo)
                }
            }
            binding.chipGroupCorreos.addView(chip)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                updateUI(uiState)
            }
        }
    }

    private fun updateUI(uiState: ActaVisitaUiState) {
        // Mostrar/ocultar loading
        if (uiState.isLoading) {
            showLoading()
        } else {
            hideLoading()
        }

        // Mostrar datos del acta
        uiState.acta?.let { acta ->
            populateActaData(acta)
        }

        // Mostrar errores
        uiState.errorMessage?.let { errorMessage ->
            showError(errorMessage)
            viewModel.clearError()
        }
        municipioAdapter.updateData(uiState.municipios)
        uiState.selectedMunicipio?.let { municipio ->
            binding.municipioExpedicion.setText(municipio.displayName, false)
        }

        // NUEVO: Actualizar chips de correos
        updateCorreosChips(uiState.correosContacto)

        // Restaurar datos del formulario guardados
        restoreFormData(uiState)
    }

    private fun populateActaData(actaEntity: ActaEntity) {
        with(binding) {
            val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
            val datetimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
            // Información del establecimiento
            nombreEst.text = actaEntity.establecimientoActa.orNA()
            departamentoEst.text = actaEntity.departamentoActa.orNA()
            municipioEst.text = actaEntity.ciudadActa.orNA()
            codigoEst.text = actaEntity.estCodigoInternoActa.orNA()
            direccionEst.text = actaEntity.direccionActa.orNA()

            fechaEst.text = safeFormatDate(actaEntity.fechaCorteInventarioActa, datetimeFormatter)

            fechaVisita.text = safeFormatDate(LocalDateTime.now(), datetimeFormatter)

            // Información del operador
            nombreOpe.text = actaEntity.nombreOperadorActa.orNA()
            nitOpe.text = actaEntity.nitActa.orNA()
            emailOpe.text = actaEntity.emailActa.orNA()

            // Información del contrato
            acta.text = getString(R.string.acta_visita_ah, actaEntity.numActa.toString())
            auto.text = getString(R.string.acta_visita_ac, actaEntity.numActa.toString())
            autoComisorio.text = actaEntity.numActa.toString()
            numContrato.text = actaEntity.numContratoActa.orNA()

            // Fecha fin contrato
            fechaFin.text = safeFormatDate(actaEntity.fechaFinContratoActa, dateFormatter)
            fechaAutoComisorio.text = safeFormatDate(actaEntity.fechaVisitaAucActa, dateFormatter)
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

    private fun restoreFormData(uiState: ActaVisitaUiState) {
        // Evitar loops infinitos verificando si el texto es diferente
        if (binding.nombrePresente.text.toString() != uiState.nombrePresente) {
            binding.nombrePresente.setText(uiState.nombrePresente)
        }
        if (binding.cedulaPresente.text.toString() != uiState.cedulaPresente) {
            binding.cedulaPresente.setText(uiState.cedulaPresente)
        }
        if (binding.calidad.text.toString() != uiState.cargoPresente) {
            binding.calidad.setText(uiState.cargoPresente)
        }
        if (binding.email.text.toString() != uiState.emailPresente) {
            binding.email.setText(uiState.emailPresente)
        }
    }

    private fun safeFormatDate(date: LocalDateTime?, formatter: DateTimeFormatter): String =
        runCatching { formatter.format(date) }.getOrElse { "N/A" }

    private fun safeFormatDate(date: LocalDate?, formatter: DateTimeFormatter): String =
        runCatching { formatter.format(date) }.getOrElse { "N/A" }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}