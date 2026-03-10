package com.coljuegos.sivo.ui.establecimiento.juegoresponsable

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.coljuegos.sivo.R
import com.coljuegos.sivo.databinding.FragmentVerificacionJuegoResponsableBinding
import com.coljuegos.sivo.ui.establecimiento.siplaft.VerificacionSiplaftFragmentDirections
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VerificacionJuegoResponsableFragment : Fragment() {

    private var _binding: FragmentVerificacionJuegoResponsableBinding? = null

    private val binding get() = _binding!!

    private val args: VerificacionJuegoResponsableFragmentArgs by navArgs()

    private val viewModel: VerificacionJuegoResponsableViewModel by viewModels()

    private lateinit var adapterSiNoNa: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerificacionJuegoResponsableBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        setupSpinners()
        setupButtons()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        // Registrar listener cuando el fragment es visible
        parentFragmentManager.setFragmentResultListener("camera_action", viewLifecycleOwner) { _, _ ->
            Log.d("VerificacionJuegoResponsableFragment", "Recibido evento de cámara")
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

        binding.pregunta1Spinner.setAdapter(adapterSiNoNa)
        binding.pregunta2Spinner.setAdapter(adapterSiNoNa)
        binding.pregunta3Spinner.setAdapter(adapterSiNoNa)

        binding.pregunta1Spinner.setOnItemClickListener { _, _, position, _ ->
            val selectedValue = adapterSiNoNa.getItem(position) ?: ""
            viewModel.updateCuentaTestIdentificacionRiesgos(selectedValue)
        }
        binding.pregunta2Spinner.setOnItemClickListener { _, _, position, _ ->
            val selectedValue = adapterSiNoNa.getItem(position) ?: ""
            viewModel.updateExistenPiezasPublicitarias(selectedValue)
        }
        binding.pregunta3Spinner.setOnItemClickListener { _, _, position, _ ->
            val selectedValue = adapterSiNoNa.getItem(position) ?: ""
            viewModel.updateCuentaProgramaJuegoResponsable(selectedValue)
        }
    }

    private fun setupButtons() {
        binding.btnAnterior.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.btnSiguiente.setOnClickListener {
            if (validateFields()) {
                val currentState = viewModel.uiState.value
                currentState.actaUuid?.let { uuid ->
                    val action = VerificacionJuegoResponsableFragmentDirections
                        .actionVerificacionJuegoResponsableFragmentToInventarioFragment(uuid)
                    findNavController().navigate(action)
                }
            }
        }
    }

    private fun validateFields(): Boolean {
        val state = viewModel.uiState.value
        var isValid = true
        var firstErrorField: View? = null

        binding.pregunta1Layout.error = null
        binding.pregunta2Layout.error = null
        binding.pregunta3Layout.error = null

        if (state.cuentaTestIdentificacionRiesgos.isBlank()) {
            binding.pregunta1Layout.error = getString(R.string.juego_responsable_validacion_pregunta1)
            isValid = false
            firstErrorField = binding.pregunta1Layout
        }
        if (state.existenPiezasPublicitarias.isBlank()) {
            binding.pregunta2Layout.error = getString(R.string.juego_responsable_validacion_pregunta2)
            isValid = false
            if (firstErrorField == null) firstErrorField = binding.pregunta2Layout
        }
        if (state.cuentaProgramaJuegoResponsable.isBlank()) {
            binding.pregunta3Layout.error = getString(R.string.juego_responsable_validacion_pregunta3)
            isValid = false
            if (firstErrorField == null) firstErrorField = binding.pregunta3Layout
        }

        if (!isValid) {
            Snackbar.make(
                binding.root,
                getString(R.string.juego_responsable_validacion_campos_incompletos),
                Snackbar.LENGTH_LONG
            ).show()
            firstErrorField?.let { field ->
                binding.root.post {
                    val scrollView = binding.root.parent as? ScrollView
                    scrollView?.smoothScrollTo(0, field.top)
                }
            }
        }
        return isValid
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                if (state.cuentaTestIdentificacionRiesgos.isNotBlank() &&
                    binding.pregunta1Spinner.text.toString() != state.cuentaTestIdentificacionRiesgos
                ) {
                    binding.pregunta1Spinner.setText(state.cuentaTestIdentificacionRiesgos, false)
                }
                if (state.existenPiezasPublicitarias.isNotBlank() &&
                    binding.pregunta2Spinner.text.toString() != state.existenPiezasPublicitarias
                ) {
                    binding.pregunta2Spinner.setText(state.existenPiezasPublicitarias, false)
                }
                if (state.cuentaProgramaJuegoResponsable.isNotBlank() &&
                    binding.pregunta3Spinner.text.toString() != state.cuentaProgramaJuegoResponsable
                ) {
                    binding.pregunta3Spinner.setText(state.cuentaProgramaJuegoResponsable, false)
                }
            }
        }
    }

    private fun navigateToGallery() {
        val currentState = viewModel.uiState.value
        currentState.actaUuid?.let { acta ->
            val action = VerificacionJuegoResponsableFragmentDirections.actionVerificacionJuegoResponsableFragmentToGalleryFragment(acta, "verificacion_responsable")
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}