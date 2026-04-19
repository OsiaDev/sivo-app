package com.coljuegos.sivo.ui.establecimiento.bingo

import android.os.Bundle
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
        var isValid = true
        var firstError: View? = null

        binding.sistemaTecnologicoLayout.error = null
        binding.sistemaInterconectadoLayout.error = null
        binding.eventosEspecialesLayout.error = null
        binding.tipoBaloterLayout.error = null
        binding.valorCartonExpuestoLayout.error = null

        val s = viewModel.uiState.value

        if (s.sistemaTecnologico.isBlank()) {
            binding.sistemaTecnologicoLayout.error = getString(R.string.campo_requerido)
            isValid = false; if (firstError == null) firstError = binding.sistemaTecnologicoLayout
        }
        if (s.sistemaInterconectado.isBlank()) {
            binding.sistemaInterconectadoLayout.error = getString(R.string.campo_requerido)
            isValid = false; if (firstError == null) firstError = binding.sistemaInterconectadoLayout
        }
        if (s.realizaEventosEspeciales.isBlank()) {
            binding.eventosEspecialesLayout.error = getString(R.string.campo_requerido)
            isValid = false; if (firstError == null) firstError = binding.eventosEspecialesLayout
        }
        if (s.tipoBalotera.isBlank()) {
            binding.tipoBaloterLayout.error = getString(R.string.campo_requerido)
            isValid = false; if (firstError == null) firstError = binding.tipoBaloterLayout
        }
        if (s.valorCartonExpuesto.isBlank()) {
            binding.valorCartonExpuestoLayout.error = getString(R.string.campo_requerido)
            isValid = false; if (firstError == null) firstError = binding.valorCartonExpuestoLayout
        }

        if (!isValid) {
            Snackbar.make(binding.root, getString(R.string.validacion_campos_incompletos), Snackbar.LENGTH_LONG).show()
            firstError?.let { binding.root.post { (binding.root.parent as? android.widget.ScrollView)?.smoothScrollTo(0, it.top) } }
        }
        return isValid
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
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