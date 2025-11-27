package com.coljuegos.sivo.ui.establecimiento.novedad

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
import com.coljuegos.sivo.databinding.FragmentRegistrarNovedadBinding
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
        // Mostrar/ocultar campo de serial según checkbox de tiene placa
        binding.valorTienePlacaCheckbox.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutSerialTitle.isVisible = isChecked
            binding.valorSerialInputLayout.isVisible = isChecked

            // Limpiar el campo de serial cuando se oculta
            if (!isChecked) {
                binding.valorSerialEditText.setText("")
                binding.valorSerialInputLayout.error = null
            }
        }

        // Mostrar/ocultar secciones de contadores según el estado
        binding.operandoSpinner.setOnItemClickListener { _, _, position, _ ->
            val selectedValue = adapterOperando.getItem(position) ?: ""

            // Si está "Operando", mostrar los contadores
            val mostrarContadores = selectedValue == "Operando"
            binding.contadoresMetTitle.isVisible = mostrarContadores
            binding.layoutContadoresMet.isVisible = mostrarContadores
            binding.contadoresSclmTitle.isVisible = mostrarContadores
            binding.layoutContadoresSclm.isVisible = mostrarContadores
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
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: RegistrarNovedadUiState) {
        // Mostrar/ocultar loading
        binding.progressIndicator.isVisible = state.isLoading

        // Si es modo edición, cargar datos de la novedad existente
        state.novedadRegistrada?.let { novedad ->
            // NUEVO: Configurar checkbox de tiene placa
            binding.valorTienePlacaCheckbox.isChecked = novedad.tienePlaca
            binding.valorSerialEditText.setText(novedad.serial)
            binding.valorMarcaEditText.setText(novedad.marca)
            binding.valorCodigoApuestaEditText.setText(novedad.codigoApuesta)
            binding.operandoSpinner.setText(novedad.operando, false)
            // Valor de crédito
            binding.valorCreditoEditText.setText(novedad.valorCredito ?: "")

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

            // Mostrar contadores si está operando
            val mostrarContadores = novedad.operando == "Operando"
            binding.contadoresMetTitle.isVisible = mostrarContadores
            binding.layoutContadoresMet.isVisible = mostrarContadores
            binding.contadoresSclmTitle.isVisible = mostrarContadores
            binding.layoutContadoresSclm.isVisible = mostrarContadores
        }

        // Si es creación, cargar datos del inventario de referencia
        if (!state.esEdicion) {
            state.novedadRegistrada?.let { novedad ->
                // Prellenar con datos del inventario si existen
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

        // Cambiar texto del botón según modo
        if (state.esEdicion) {
            binding.btnRegistrar.text = "Actualizar"
        } else {
            binding.btnRegistrar.text = getString(R.string.registrar_inventario_btn_registrar)
        }

        // Mostrar errores
        state.errorMessage?.let { error ->
            Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
            viewModel.clearError()
        }

        // Navegar atrás si se guardó exitosamente
        if (state.guardadoExitoso) {
            val mensaje = if (state.esEdicion) {
                "Novedad actualizada exitosamente"
            } else {
                "Novedad registrada exitosamente"
            }

            Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_SHORT).show()

            // Pequeño delay para que se vea el mensaje antes de navegar
            view?.postDelayed({
                findNavController().navigateUp()
            }, 500)
        }
    }

    private fun guardarNovedad() {
        val tienePlaca = binding.valorTienePlacaCheckbox.isChecked
        val serial = binding.valorSerialEditText.text?.toString() ?: ""
        val marca = binding.valorMarcaEditText.text?.toString() ?: ""
        val codigoApuesta = binding.valorCodigoApuestaEditText.text?.toString() ?: ""
        val operando = binding.operandoSpinner.text?.toString() ?: ""

        val valorCredito = binding.valorCreditoEditText.text?.toString()

        // Contadores MET (solo si está operando)
        val coinInMet = binding.coinInMetEditText.text?.toString()
        val coinOutMet = binding.coinOutMetEditText.text?.toString()
        val jackpotMet = binding.jackpotMetEditText.text?.toString()

        // Contadores SCLM (solo si está operando)
        val coinInSclm = binding.coinInSclmEditText.text?.toString()
        val coinOutSclm = binding.coinOutSclmEditText.text?.toString()
        val jackpotSclm = binding.jackpotSclmEditText.text?.toString()

        // Observaciones
        val observaciones = binding.observacionesEditText.text?.toString()

        // Validaciones básicas de UI
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

        if (!isValid) {
            return
        }

        // Guardar en el ViewModel (donde se hace la validación de serial duplicado)
        viewModel.guardarNovedad(
            serial = serial,
            marca = marca,
            codigoApuesta = codigoApuesta,
            operando = operando,
            tienePlaca = tienePlaca,
            valorCredito = valorCredito,
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}