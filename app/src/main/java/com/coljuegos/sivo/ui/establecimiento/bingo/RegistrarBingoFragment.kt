package com.coljuegos.sivo.ui.establecimiento.bingo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.coljuegos.sivo.R
import com.coljuegos.sivo.data.entity.EstadoInventarioEnum
import com.coljuegos.sivo.databinding.FragmentRegistrarBingoBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegistrarBingoFragment : Fragment() {

    private var _binding: FragmentRegistrarBingoBinding? = null
    private val binding get() = _binding!!

    private val args: RegistrarBingoFragmentArgs by navArgs()
    private val viewModel: RegistrarBingoViewModel by viewModels()

    private lateinit var adapterEstado: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrarBingoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        setupSpinners()
        setupCheckboxListeners()
        setupButtons()
        observeViewModel()
        viewModel.loadDatos(args.actaUuid, args.inventarioUuid, args.inventarioBingoRegistradoUuid)
    }

    override fun onResume() {
        super.onResume()
        binding.estadoSpinner.setAdapter(adapterEstado)
        val s = viewModel.uiState.value
        s.registro?.let { reg ->
            val estadoStr = resources.getStringArray(R.array.estado_options)
                .firstOrNull { it == EstadoInventarioEnum.toString(reg.estado) } ?: ""
            if (binding.estadoSpinner.text.toString() != estadoStr && estadoStr.isNotBlank()) {
                binding.estadoSpinner.setText(estadoStr, false)
            }
        }
    }

    private fun setupAdapters() {
        adapterEstado = ArrayAdapter(
            requireContext(),
            R.layout.item_dropdown,
            resources.getStringArray(R.array.estado_options)
        )
    }

    private fun setupSpinners() {
        binding.estadoSpinner.setAdapter(adapterEstado)
        binding.estadoSpinner.setOnItemClickListener { _, _, _, _ ->
            // No hay lógica adicional por ahora
        }
    }

    private fun setupCheckboxListeners() {
        binding.codigoApuestaDiferenteCheckbox.setOnCheckedChangeListener { _, isChecked ->
            binding.codigoApuestaDiferenteInputLayout.isVisible = isChecked
            if (!isChecked) binding.codigoApuestaDiferenteEditText.text?.clear()
        }
        binding.cantidadSillasDiferenteCheckbox.setOnCheckedChangeListener { _, isChecked ->
            binding.cantidadSillasDiferenteInputLayout.isVisible = isChecked
            if (!isChecked) binding.cantidadSillasDiferenteEditText.text?.clear()
        }
    }

    private fun setupButtons() {
        binding.btnCancelar.setOnClickListener { findNavController().navigateUp() }
        binding.btnRegistrar.setOnClickListener { guardar() }
    }

    private fun guardar() {
        // Validar estado
        if (binding.estadoSpinner.text.isNullOrBlank()) {
            Snackbar.make(binding.root, "Debe seleccionar un estado", Snackbar.LENGTH_LONG).show()
            return
        }
        binding.estadoLayout.error = null

        val codigoDiferente = binding.codigoApuestaDiferenteCheckbox.isChecked
        val codigoDiferenteValor = binding.codigoApuestaDiferenteEditText.text?.toString()?.trim()

        if (codigoDiferente && codigoDiferenteValor.isNullOrBlank()) {
            Snackbar.make(binding.root, "Debe ingresar un código de apuesta diferente", Snackbar.LENGTH_LONG).show()
            return
        }
        binding.codigoApuestaDiferenteInputLayout.error = null

        val sillasDiferente = binding.cantidadSillasDiferenteCheckbox.isChecked
        val sillasValorStr = binding.cantidadSillasDiferenteEditText.text?.toString()?.trim()
        val sillasValor = sillasValorStr?.toIntOrNull()

        if (sillasDiferente && sillasValor == null) {
            binding.cantidadSillasDiferenteInputLayout.error = getString(R.string.registrar_bingo_sillas_valor_requerido)
            return
        }
        binding.cantidadSillasDiferenteInputLayout.error = null

        val estadoStr = binding.estadoSpinner.text.toString()
        val estado = EstadoInventarioEnum.fromString(estadoStr)
        val observaciones = binding.observacionesEditText.text?.toString()?.trim()

        viewModel.guardar(
            codigoApuestaDiferente = codigoDiferente,
            codigoApuestaDiferenteValor = codigoDiferenteValor,
            sillasDiferente = sillasDiferente,
            sillasValor = sillasValor,
            estado = estado,
            observaciones = observaciones
        )
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                if (state.guardadoExitoso) {
                    findNavController().navigateUp()
                    return@collect
                }

                state.inventario?.let { inv ->
                    binding.codigoApuestaValue.text = inv.codigoTipoApuestaInventario
                    binding.cantidadSillasValue.text = inv.invSillasInventario.toString()
                }

                state.registro?.let { reg ->
                    binding.codigoApuestaDiferenteCheckbox.isChecked = reg.codigoApuestaDiferente
                    binding.codigoApuestaDiferenteInputLayout.isVisible = reg.codigoApuestaDiferente
                    binding.codigoApuestaDiferenteEditText.setText(reg.codigoApuestaDiferenteValor ?: "")

                    binding.cantidadSillasDiferenteCheckbox.isChecked = reg.sillasDiferente
                    binding.cantidadSillasDiferenteInputLayout.isVisible = reg.sillasDiferente
                    binding.cantidadSillasDiferenteEditText.setText(reg.sillasValor?.toString() ?: "")

                    binding.observacionesEditText.setText(reg.observaciones ?: "")
                }

                state.errorMessage?.let {
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}