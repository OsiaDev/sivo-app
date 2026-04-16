package com.coljuegos.sivo.ui.establecimiento.novedad

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.coljuegos.sivo.R
import com.coljuegos.sivo.databinding.FragmentRegistrarNovedadBinding
import com.coljuegos.sivo.ui.establecimiento.novedad.RegistrarNovedadFragmentDirections
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegistrarNovedadFragment : Fragment() {

    private var _binding: FragmentRegistrarNovedadBinding? = null

    private val binding get() = _binding!!

    private val args: RegistrarNovedadFragmentArgs by navArgs()

    private val viewModel: RegistrarNovedadViewModel by viewModels()

    private lateinit var adapterOperando: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrarNovedadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        setupSpinners()
        setupButtons()
        setupVisibilityListeners()
        setupContadoresMirrorListeners()
        observeViewModel()
    }

    private fun setupAdapters() {
        // Adapter para Operando/Apagado
        val opcionesOperando = resources.getStringArray(R.array.operando_options)
        adapterOperando = ArrayAdapter(
            requireContext(),
            R.layout.item_dropdown,
            opcionesOperando
        )
    }

    private fun setupSpinners() {
        binding.operandoSpinner.setAdapter(adapterOperando)
    }

    private fun setupVisibilityListeners() {
        // Estado inicial del botón de fotografía
        actualizarEstadoBotonFotografia()

        // Mostrar/ocultar campo de serial según checkbox de tiene placa
        binding.valorTienePlacaCheckbox.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutSerialTitle.isVisible = isChecked
            binding.valorSerialInputLayout.isVisible = isChecked

            // Limpiar el campo de serial cuando se oculta
            if (!isChecked) {
                binding.valorSerialEditText.setText("")
                binding.valorSerialInputLayout.error = null
            }
            
            actualizarEstadoBotonFotografia()
        }

        // Monitorear cambios en el texto del serial
        binding.valorSerialEditText.doAfterTextChanged {
            actualizarEstadoBotonFotografia()
        }

        // Mostrar/ocultar secciones de contadores según el estado
        binding.operandoSpinner.setOnItemClickListener { _, _, position, _ ->
            val selectedValue = adapterOperando.getItem(position) ?: ""
            val estaOperando = selectedValue == "Operando"
            // SCLM siempre visible cuando está Operando
            binding.contadoresSclmTitle.isVisible = estaOperando
            binding.layoutContadoresSclm.isVisible = estaOperando
            // Fila del checkbox visible cuando está Operando
            binding.layoutTextoLegalContadores.isVisible = estaOperando
            binding.layoutCheckContadores.isVisible = estaOperando
            // MET solo si el checkbox está marcado
            if (!estaOperando) {
                binding.contadoresVerificadoCheckbox.isChecked = false
                binding.contadoresMetTitle.isVisible = false
                binding.layoutContadoresMet.isVisible = false
            }
        }

        // Checkbox contadores — controla visibilidad de MET únicamente
        binding.contadoresVerificadoCheckbox.setOnCheckedChangeListener { _, isChecked ->
            binding.contadoresMetTitle.isVisible = isChecked
            binding.layoutContadoresMet.isVisible = isChecked
        }
    }

    private fun setupContadoresMirrorListeners() {
        // Solo aplica cuando el checkbox de contadores está activo y el operando es "Operando"

        fun contadoresActivos() = binding.contadoresVerificadoCheckbox.isChecked

        // Coin In MET → Coin In SCLM
        binding.coinInMetEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && contadoresActivos()) {
                val valor = binding.coinInMetEditText.text?.toString()?.trim() ?: ""
                if (valor.isNotEmpty() && binding.coinInSclmEditText.text.isNullOrBlank()) {
                    binding.coinInSclmEditText.setText(valor)
                }
            }
        }

        // Coin Out MET → Coin Out SCLM
        binding.coinOutMetEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && contadoresActivos()) {
                val valor = binding.coinOutMetEditText.text?.toString()?.trim() ?: ""
                if (valor.isNotEmpty() && binding.coinOutSclmEditText.text.isNullOrBlank()) {
                    binding.coinOutSclmEditText.setText(valor)
                }
            }
        }

        // Jackpot MET → Jackpot SCLM
        binding.jackpotMetEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && contadoresActivos()) {
                val valor = binding.jackpotMetEditText.text?.toString()?.trim() ?: ""
                if (valor.isNotEmpty() && binding.jackpotSclmEditText.text.isNullOrBlank()) {
                    binding.jackpotSclmEditText.setText(valor)
                }
            }
        }

        // Coin In SCLM → Coin In MET (inverso)
        binding.coinInSclmEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && contadoresActivos()) {
                val valor = binding.coinInSclmEditText.text?.toString()?.trim() ?: ""
                if (valor.isNotEmpty() && binding.coinInMetEditText.text.isNullOrBlank()) {
                    binding.coinInMetEditText.setText(valor)
                }
            }
        }

        // Coin Out SCLM → Coin Out MET (inverso)
        binding.coinOutSclmEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && contadoresActivos()) {
                val valor = binding.coinOutSclmEditText.text?.toString()?.trim() ?: ""
                if (valor.isNotEmpty() && binding.coinOutMetEditText.text.isNullOrBlank()) {
                    binding.coinOutMetEditText.setText(valor)
                }
            }
        }

        // Jackpot SCLM → Jackpot MET (inverso)
        binding.jackpotSclmEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && contadoresActivos()) {
                val valor = binding.jackpotSclmEditText.text?.toString()?.trim() ?: ""
                if (valor.isNotEmpty() && binding.jackpotMetEditText.text.isNullOrBlank()) {
                    binding.jackpotMetEditText.setText(valor)
                }
            }
        }
    }

    private fun setupButtons() {
        // Botón Cancelar
        binding.btnCancelar.setOnClickListener {
            mostrarDialogoCancelar()
        }

        // Botón Registrar (se mostrará como "Actualizar" en modo edición)
        binding.btnRegistrar.setOnClickListener {
            guardarNovedad()
        }

        binding.btnCapturarMaquina.setOnClickListener {
            val serialStr = binding.valorSerialEditText.text?.toString()?.takeIf { it.isNotBlank() }
            val serial = serialStr?.replace(" ", "_")
                ?: viewModel.uiState.value.novedadRegistrada?.serial?.replace(" ", "_")
                ?: "sin_serial"
            val action = RegistrarNovedadFragmentDirections
                .actionRegistrarNovedadFragmentToGalleryFragment(
                    actaUuid = args.actaUuid,
                    fragmentOrigen = "maquina_$serial"
                )
            findNavController().navigate(action)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: RegistrarNovedadUiState) {
        binding.progressIndicator.isVisible = state.isLoading

        state.novedadRegistrada?.let { novedad ->
            binding.valorTienePlacaCheckbox.isChecked = novedad.tienePlaca
            binding.descripcionJuegoCheckbox.isChecked = novedad.descripcionJuego
            binding.planPremiosCheckbox.isChecked = novedad.planPremios
            binding.valorPremiosCheckbox.isChecked = novedad.valorPremios
            binding.valorSerialEditText.setText(novedad.serial)
            binding.valorMarcaEditText.setText(novedad.marca)
            binding.valorCodigoApuestaEditText.setText(novedad.codigoApuesta)
            binding.operandoSpinner.setText(novedad.operando, false)
            binding.valorCreditoEditText.setText(novedad.valorCredito ?: "")
            binding.numeroInternoMetOperadorEditText.setText(novedad.numeroInternoMet ?: "")

            // Contadores MET
            binding.coinInMetEditText.setText(novedad.coinInMet ?: "")
            binding.coinOutMetEditText.setText(novedad.coinOutMet ?: "")
            binding.jackpotMetEditText.setText(novedad.jackpotMet ?: "")

            // Contadores SCLM
            binding.coinInSclmEditText.setText(novedad.coinInSclm ?: "")
            binding.coinOutSclmEditText.setText(novedad.coinOutSclm ?: "")
            binding.jackpotSclmEditText.setText(novedad.jackpotSclm ?: "")

            // Observaciones
            binding.observacionesEditText.setText(novedad.observaciones ?: "")

            // Mostrar/ocultar secciones según si está operando
            val estaOperando = novedad.operando == "Operando"
            // SCLM siempre visible si está Operando
            binding.contadoresSclmTitle.isVisible = estaOperando
            binding.layoutContadoresSclm.isVisible = estaOperando
            // Fila checkbox visible si está Operando
            binding.layoutTextoLegalContadores.isVisible = estaOperando
            binding.layoutCheckContadores.isVisible = estaOperando
            // MET visible solo si el checkbox está marcado
            binding.contadoresVerificadoCheckbox.isChecked = novedad.contadoresVerificado
            binding.contadoresMetTitle.isVisible = estaOperando && novedad.contadoresVerificado
            binding.layoutContadoresMet.isVisible = estaOperando && novedad.contadoresVerificado
        }

        if (!state.esEdicion) {
            state.novedadRegistrada?.let { novedad ->
                if (binding.valorSerialEditText.text.isNullOrBlank()) {
                    binding.valorSerialEditText.setText(novedad.serial)
                }
                if (binding.valorMarcaEditText.text.isNullOrBlank()) {
                    binding.valorMarcaEditText.setText(novedad.marca)
                }
                if (binding.valorCodigoApuestaEditText.text.isNullOrBlank()) {
                    binding.valorCodigoApuestaEditText.setText(novedad.codigoApuesta)
                }
            }
        }

        if (state.esEdicion) {
            binding.btnRegistrar.text = "Actualizar"
        } else {
            binding.btnRegistrar.text = getString(R.string.registrar_inventario_btn_registrar)
        }

        state.errorMessage?.let { error ->
            Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
            viewModel.clearError()
        }

        if (state.guardadoExitoso) {
            val mensaje = if (state.esEdicion) "Novedad actualizada exitosamente"
            else "Novedad registrada exitosamente"
            Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_SHORT).show()
            view?.postDelayed({ findNavController().navigateUp() }, 500)
        }
    }

    private fun guardarNovedad() {
        val tienePlaca = binding.valorTienePlacaCheckbox.isChecked
        val descripcionJuego = binding.descripcionJuegoCheckbox.isChecked
        val planPremios = binding.planPremiosCheckbox.isChecked
        val valorPremios = binding.valorPremiosCheckbox.isChecked
        val contadoresVerificado = binding.contadoresVerificadoCheckbox.isChecked
        val serial = binding.valorSerialEditText.text?.toString() ?: ""
        val marca = binding.valorMarcaEditText.text?.toString() ?: ""
        val codigoApuesta = binding.valorCodigoApuestaEditText.text?.toString() ?: ""
        val operando = binding.operandoSpinner.text?.toString() ?: ""
        val valorCredito = binding.valorCreditoEditText.text?.toString()
        val numeroInternoMet = binding.numeroInternoMetOperadorEditText.text?.toString()
        val coinInMet = binding.coinInMetEditText.text?.toString()
        val coinOutMet = binding.coinOutMetEditText.text?.toString()
        val jackpotMet = binding.jackpotMetEditText.text?.toString()
        val coinInSclm = binding.coinInSclmEditText.text?.toString()
        val coinOutSclm = binding.coinOutSclmEditText.text?.toString()
        val jackpotSclm = binding.jackpotSclmEditText.text?.toString()
        val observaciones = binding.observacionesEditText.text?.toString()

        var isValid = true
        if (tienePlaca && serial.isBlank()) {
            binding.valorSerialInputLayout.error = "Campo obligatorio"
            isValid = false
        } else {
            binding.valorSerialInputLayout.error = null
        }
        if (marca.isBlank()) {
            binding.valorMarcaInputLayout.error = "Campo obligatorio"
            isValid = false
        } else {
            binding.valorMarcaInputLayout.error = null
        }
        if (operando.isBlank()) {
            binding.operandoLayout.error = "Campo obligatorio"
            isValid = false
        } else {
            binding.operandoLayout.error = null
        }
        if (!isValid) return

        viewModel.guardarNovedad(
            serial = serial,
            marca = marca,
            codigoApuesta = codigoApuesta,
            operando = operando,
            tienePlaca = tienePlaca,
            descripcionJuego = descripcionJuego,
            planPremios = planPremios,
            valorPremios = valorPremios,
            contadoresVerificado = contadoresVerificado,
            valorCredito = valorCredito,
            numeroInternoMet = numeroInternoMet,
            coinInMet = coinInMet,
            coinOutMet = coinOutMet,
            jackpotMet = jackpotMet,
            coinInSclm = coinInSclm,
            coinOutSclm = coinOutSclm,
            jackpotSclm = jackpotSclm,
            observaciones = observaciones
        )
    }

    private fun mostrarDialogoCancelar() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cancelar registro")
            .setMessage("¿Está seguro que desea cancelar? Los cambios no guardados se perderán.")
            .setPositiveButton("Sí, cancelar") { _, _ ->
                findNavController().navigateUp()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun actualizarEstadoBotonFotografia() {
        val tienePlaca = binding.valorTienePlacaCheckbox.isChecked
        val serialDigitado = binding.valorSerialEditText.text?.toString() ?: ""
        
        // Habilitar si la máquina no tiene placa, o si tiene placa y se ingresó el serial
        binding.btnCapturarMaquina.isEnabled = !tienePlaca || serialDigitado.isNotBlank()
    }

    override fun onResume() {
        super.onResume()
        binding.operandoSpinner.setAdapter(adapterOperando)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}