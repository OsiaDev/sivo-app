package com.coljuegos.sivo.ui.establecimiento.bingo

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
import androidx.navigation.fragment.navArgs
import com.coljuegos.sivo.R
import com.coljuegos.sivo.databinding.FragmentVerificacionBingoBinding
import com.coljuegos.sivo.ui.establecimiento.verificacion.VerificacionContractualFragmentDirections
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VerificacionBingoFragment : Fragment() {

    private var _binding: FragmentVerificacionBingoBinding? = null

    private val binding get() = _binding!!

    private val args: VerificacionBingoFragmentArgs by navArgs()

    private val viewModel: VerificacionBingoViewModel by viewModels()

    private fun makeAdapterSiNoNa(): ArrayAdapter<String> {
        val opciones = resources.getStringArray(R.array.si_no_na_options)
        return object : ArrayAdapter<String>(requireContext(), R.layout.item_dropdown, opciones) {
            override fun getFilter() = object : android.widget.Filter() {
                override fun performFiltering(c: CharSequence?) =
                    FilterResults().apply { values = opciones; count = opciones.size }
                override fun publishResults(c: CharSequence?, r: FilterResults?) =
                    notifyDataSetChanged()
            }
        }
    }

    private fun makeAdapterBalotera(): ArrayAdapter<String> {
        val opciones = resources.getStringArray(R.array.balotera_options)
        return object : ArrayAdapter<String>(requireContext(), R.layout.item_dropdown, opciones) {
            override fun getFilter() = object : android.widget.Filter() {
                override fun performFiltering(c: CharSequence?) =
                    FilterResults().apply { values = opciones; count = opciones.size }
                override fun publishResults(c: CharSequence?, r: FilterResults?) =
                    notifyDataSetChanged()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerificacionBingoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSpinners()
        setupTextListeners()
        setupButtons()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        parentFragmentManager.setFragmentResultListener("camera_action", viewLifecycleOwner) { _, _ ->
            Log.d("ActaVisitaFragment", "Recibido evento de cámara")
            navigateToGallery()
        }
        val adSiNoNa = makeAdapterSiNoNa()
        val adBalotera = makeAdapterBalotera()

        binding.sistemaTecnologicoSpinner.setAdapter(adSiNoNa)
        binding.sistemaInterconectadoSpinner.setAdapter(adSiNoNa)
        binding.eventosEspecialesSpinner.setAdapter(adSiNoNa)
        binding.valorCartonExpuestoSpinner.setAdapter(adSiNoNa)
        binding.tipoBaloterSpinner.setAdapter(adBalotera)

        // Restaurar valores tras setAdapter
        val s = viewModel.uiState.value
        restoreSpinner(binding.sistemaTecnologicoSpinner, s.sistemaTecnologico)
        restoreSpinner(binding.sistemaInterconectadoSpinner, s.sistemaInterconectado)
        restoreSpinner(binding.eventosEspecialesSpinner, s.realizaEventosEspeciales)
        restoreSpinner(binding.valorCartonExpuestoSpinner, s.valorCartonExpuesto)
        restoreSpinner(binding.tipoBaloterSpinner, s.tipoBalotera)
    }

    override fun onPause() {
        super.onPause()
        // Limpiar listener cuando el fragment no es visible
        parentFragmentManager.clearFragmentResultListener("camera_action")
    }

    private fun navigateToGallery() {
        val currentState = viewModel.uiState.value
        currentState.actaUuid?.let { acta ->
            val action = VerificacionBingoFragmentDirections.actionVerificacionBingoFragmentToGalleryFragment(acta, "verificacion_bingo")
            findNavController().navigate(action)
        }
    }

    private fun restoreSpinner(spinner: android.widget.AutoCompleteTextView, value: String) {
        if (value.isNotBlank() && spinner.text.toString() != value) {
            spinner.setText(value, false)
        }
    }

    private fun setupSpinners() {
        binding.sistemaTecnologicoSpinner.setOnItemClickListener { _, _, _, _ ->
            viewModel.updateSistemaTecnologico(binding.sistemaTecnologicoSpinner.text.toString())
        }
        binding.sistemaInterconectadoSpinner.setOnItemClickListener { _, _, _, _ ->
            viewModel.updateSistemaInterconectado(binding.sistemaInterconectadoSpinner.text.toString())
        }
        binding.eventosEspecialesSpinner.setOnItemClickListener { _, _, _, _ ->
            viewModel.updateRealizaEventosEspeciales(binding.eventosEspecialesSpinner.text.toString())
        }
        binding.tipoBaloterSpinner.setOnItemClickListener { _, _, _, _ ->
            viewModel.updateTipoBalotera(binding.tipoBaloterSpinner.text.toString())
        }
        binding.valorCartonExpuestoSpinner.setOnItemClickListener { _, _, _, _ ->
            viewModel.updateValorCartonExpuesto(binding.valorCartonExpuestoSpinner.text.toString())
        }
    }

    private fun setupTextListeners() {
        binding.cartonesModulosEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateCartonesModulos(text?.toString() ?: "")
        }
    }

    private fun setupButtons() {
        binding.btnAnterior.setOnClickListener { findNavController().navigateUp() }

        binding.btnSiguiente.setOnClickListener {
            if (validateRequiredFields()) {
                val action = VerificacionBingoFragmentDirections
                    .actionVerificacionBingoFragmentToInventarioBingoReportadoFragment(args.actaUuid)
                findNavController().navigate(action)
            }
        }
    }

    private fun validateRequiredFields(): Boolean {
        val s = viewModel.uiState.value
        var firstError: View? = null

        binding.sistemaTecnologicoLayout.error = null
        binding.sistemaInterconectadoLayout.error = null
        binding.eventosEspecialesLayout.error = null
        binding.tipoBaloterLayout.error = null
        binding.valorCartonExpuestoLayout.error = null

        if (s.sistemaTecnologico.isBlank()) {
            binding.sistemaTecnologicoLayout.error = " "
            if (firstError == null) firstError = binding.sistemaTecnologicoLayout
        }
        if (s.sistemaInterconectado.isBlank()) {
            binding.sistemaInterconectadoLayout.error = " "
            if (firstError == null) firstError = binding.sistemaInterconectadoLayout
        }
        if (s.realizaEventosEspeciales.isBlank()) {
            binding.eventosEspecialesLayout.error = " "
            if (firstError == null) firstError = binding.eventosEspecialesLayout
        }
        if (s.tipoBalotera.isBlank()) {
            binding.tipoBaloterLayout.error = " "
            if (firstError == null) firstError = binding.tipoBaloterLayout
        }
        if (s.valorCartonExpuesto.isBlank()) {
            binding.valorCartonExpuestoLayout.error = " "
            if (firstError == null) firstError = binding.valorCartonExpuestoLayout
        }

        if (firstError != null) {
            Snackbar.make(
                binding.root,
                "Por favor complete todos los campos antes de continuar",
                Snackbar.LENGTH_LONG
            ).show()
            firstError?.let { field ->
                binding.root.post {
                    val scrollView = binding.root.parent as? android.widget.ScrollView
                    scrollView?.smoothScrollTo(0, field.top)
                    field.requestFocus()
                }
            }
            return false
        }

        return true
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->

                // Restaurar spinners al recibir estado (cubre primer render y rotación)
                restoreSpinner(binding.sistemaTecnologicoSpinner, state.sistemaTecnologico)
                restoreSpinner(binding.sistemaInterconectadoSpinner, state.sistemaInterconectado)
                restoreSpinner(binding.eventosEspecialesSpinner, state.realizaEventosEspeciales)
                restoreSpinner(binding.valorCartonExpuestoSpinner, state.valorCartonExpuesto)
                restoreSpinner(binding.tipoBaloterSpinner, state.tipoBalotera)

                state.errorMessage?.let {
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                    viewModel.clearError()
                }
                if (binding.cartonesModulosEditText.text.toString() != state.cartonesModulos) {
                    binding.cartonesModulosEditText.setText(state.cartonesModulos)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}