package com.coljuegos.sivo.ui.establecimiento.inventario

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
import com.coljuegos.sivo.databinding.FragmentRegistrarInventarioBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegistrarInventarioFragment : Fragment() {

    private var _binding: FragmentRegistrarInventarioBinding? = null

    private val binding get() = _binding!!

    private val args: RegistrarInventarioFragmentArgs by navArgs()

    private val viewModel: RegistrarInventarioViewModel by viewModels()

    private lateinit var adapterEstado: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrarInventarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        setupSpinners()
        setupUI()
        setupButtons()
        setupCheckboxListeners()
        observeViewModel()

        // Cargar datos del inventario
        viewModel.loadInventario(args.actaUuid, args.inventarioUuid, args.inventarioRegistradoUuid)
    }

    private fun setupUI() {
        // Ocultar inicialmente los campos de contadores MET
        hideContadoresMetFields()
        // Mostrar siempre los campos SCLM
        showContadoresSclmFields()
        // Ocultar inicialmente el campo de código de apuesta diferente
        hideCodigoApuestaDiferenteField()
    }

    private fun setupAdapters() {
        // Adapter para estados
        val opcionesEstado = resources.getStringArray(R.array.estado_options)
        adapterEstado = ArrayAdapter(
            requireContext(),
            R.layout.item_dropdown,
            opcionesEstado
        )
    }

    private fun setupSpinners() {
        binding.estadoSpinner.setAdapter(adapterEstado)

        // Listener para el spinner de estado
        binding.estadoSpinner.setOnItemClickListener { _, _, _, _ ->
            val estadoSeleccionado = binding.estadoSpinner.text.toString()
            if (estadoSeleccionado == "No encontrado") {
                clearAllFieldsExceptObservaciones()
                hideAllFieldsExceptObservaciones()
                // Forzar ocultación de campos condicionales por si los listeners los mostraron
                binding.codigoApuestaDiferenteInputLayout.isVisible = false
                binding.serialDiferenteInputLayout.isVisible = false
            } else {
                showAllFieldsExceptObservaciones()
                restoreDefaultValues()
            }
        }
    }

    private fun setupButtons() {
        binding.btnCancelar.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnRegistrar.setOnClickListener {
            guardarInventario()
        }
    }

    private fun setupCheckboxListeners() {
        // Listener para el checkbox de contadores - solo afecta a MET
        binding.contadoresCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showContadoresMetFields()
            } else {
                hideContadoresMetFields()
            }
        }
        // Listener para el checkbox de código de apuesta diferente
        binding.codigoApuestaDiferenteCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showCodigoApuestaDiferenteField()
            } else {
                hideCodigoApuestaDiferenteField()
            }
        }

        binding.serialVerificadoCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                // Si se deschequea, mostrar campo de serial diferente
                showSerialDiferenteField()
            } else {
                // Si se chequea, ocultar y limpiar campo de serial diferente
                hideSerialDiferenteField()
            }
        }

    }

    private fun clearAllFieldsExceptObservaciones() {
        // Limpiar checkboxes
        binding.codigoApuestaDiferenteCheckbox.isChecked = false
        binding.serialVerificadoCheckbox.isChecked = false
        binding.descripcionJuegoCheckbox.isChecked = false
        binding.planPremiosCheckbox.isChecked = false
        binding.valorPremiosCheckbox.isChecked = false
        binding.contadoresCheckbox.isChecked = false

        // Limpiar campos de texto
        binding.codigoApuestaDiferenteEditText.setText("")
        binding.serialDiferenteEditText.setText("")
        binding.valorCreditoEditText.setText("")

        // Limpiar contadores MET
        binding.coinInMetEditText.setText("")
        binding.coinOutMetEditText.setText("")
        binding.jackpotMetEditText.setText("")

        // Limpiar contadores SCLM
        binding.coinInSclmEditText.setText("")
        binding.coinOutSclmEditText.setText("")
        binding.jackpotSclmEditText.setText("")
    }

    private fun showContadoresMetFields() {
        binding.contadoresMetTitle.isVisible = true
        binding.layoutContadoresMet.isVisible = true
    }

    private fun hideContadoresMetFields() {
        binding.contadoresMetTitle.isVisible = false
        binding.layoutContadoresMet.isVisible = false
    }

    private fun showContadoresSclmFields() {
        binding.contadoresSclmTitle.isVisible = true
        binding.layoutContadoresSclm.isVisible = true
    }

    private fun hideContadoresSclmFields() {
        binding.contadoresSclmTitle.isVisible = false
        binding.layoutContadoresSclm.isVisible = false
    }

    private fun showAllFieldsExceptObservaciones() {
        binding.layoutTextoLegal1Title.isVisible = true
        binding.layoutCheck1Title.isVisible = true
        binding.layoutTextoLegal2Title.isVisible = true
        binding.layoutCheck2Title.isVisible = true
        binding.layoutTextoLegal34Title.isVisible = true
        binding.layoutCheck34Title.isVisible = true
        binding.layoutTextoLegal5Title.isVisible = true
        binding.layoutCheck5Title.isVisible = true
        binding.layoutTextoLegal6Title.isVisible = true
        binding.valorCreditoInputLayout.isVisible = true
        binding.layoutTextoLegal7Title.isVisible = true
        binding.layoutCheck7Title.isVisible = true
        showContadoresSclmFields()
    }

    private fun hideAllFieldsExceptObservaciones() {
        // Ocultar títulos y checkboxes
        binding.layoutTextoLegal1Title.isVisible = false
        binding.layoutCheck1Title.isVisible = false
        binding.layoutTextoLegal2Title.isVisible = false
        binding.layoutCheck2Title.isVisible = false
        binding.layoutTextoLegal34Title.isVisible = false
        binding.layoutCheck34Title.isVisible = false
        binding.layoutTextoLegal5Title.isVisible = false
        binding.layoutCheck5Title.isVisible = false
        binding.layoutTextoLegal6Title.isVisible = false
        binding.layoutTextoLegal7Title.isVisible = false
        binding.layoutCheck7Title.isVisible = false

        // Ocultar campos condicionales
        binding.codigoApuestaDiferenteInputLayout.isVisible = false
        binding.serialDiferenteInputLayout.isVisible = false
        binding.valorCreditoInputLayout.isVisible = false

        // Ocultar contadores
        hideContadoresMetFields()
        hideContadoresSclmFields()
    }

    private fun restoreDefaultValues() {
        // Restaurar checkboxes a sus valores por defecto
        binding.codigoApuestaDiferenteCheckbox.isChecked = false
        binding.serialVerificadoCheckbox.isChecked = true
        binding.descripcionJuegoCheckbox.isChecked = true
        binding.planPremiosCheckbox.isChecked = true
        binding.valorPremiosCheckbox.isChecked = true
        binding.contadoresCheckbox.isChecked = false

        // Limpiar y ocultar campos condicionales
        binding.codigoApuestaDiferenteEditText.setText("")
        binding.codigoApuestaDiferenteInputLayout.isVisible = false
        binding.serialDiferenteEditText.setText("")
        binding.serialDiferenteInputLayout.isVisible = false

        // Limpiar campos de texto
        binding.valorCreditoEditText.setText("")

        // Limpiar contadores MET
        binding.coinInMetEditText.setText("")
        binding.coinOutMetEditText.setText("")
        binding.jackpotMetEditText.setText("")

        // Limpiar contadores SCLM
        binding.coinInSclmEditText.setText("")
        binding.coinOutSclmEditText.setText("")
        binding.jackpotSclmEditText.setText("")

        // Ocultar contadores MET (dependen del checkbox)
        hideContadoresMetFields()
    }

    private fun showCodigoApuestaDiferenteField() {
        binding.codigoApuestaDiferenteInputLayout.isVisible = true
    }

    private fun hideCodigoApuestaDiferenteField() {
        binding.codigoApuestaDiferenteInputLayout.isVisible = false
        binding.codigoApuestaDiferenteEditText.setText("")
    }

    private fun showSerialDiferenteField() {
        binding.serialDiferenteInputLayout.isVisible = true
    }

    private fun hideSerialDiferenteField() {
        binding.serialDiferenteInputLayout.isVisible = false
        binding.serialDiferenteEditText.setText("")
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                updateUI(uiState)
            }
        }
    }

    private fun updateUI(uiState: RegistrarInventarioUiState) {
        // Mostrar/ocultar progress bar
        binding.progressIndicator.isVisible = uiState.isLoading

        // Cargar datos del inventario
        uiState.inventario?.let { inventario ->
            binding.marcaValue.text = inventario.nombreMarcaInventario
            binding.serialValue.text = inventario.metSerialInventario
            binding.codigoApuestaValue.text = inventario.codigoTipoApuestaInventario
            binding.nucValue.text = inventario.nucInventario
        }

        // Si es edición, cargar datos del registro
        uiState.inventarioRegistrado?.let { registro ->
            binding.codigoApuestaDiferenteCheckbox.isChecked = registro.codigoApuestaDiferente
            if (registro.codigoApuestaDiferente && !registro.codigoApuestaDiferenteValor.isNullOrEmpty()) {
                showCodigoApuestaDiferenteField()
                binding.codigoApuestaDiferenteEditText.setText(registro.codigoApuestaDiferenteValor)
            }
            binding.serialVerificadoCheckbox.isChecked = registro.serialVerificado
            if (!registro.serialVerificado && !registro.serialDiferente.isNullOrEmpty()) {
                showSerialDiferenteField()
                binding.serialDiferenteEditText.setText(registro.serialDiferente)
            }
            binding.descripcionJuegoCheckbox.isChecked = registro.descripcionJuego
            binding.planPremiosCheckbox.isChecked = registro.planPremios
            binding.valorPremiosCheckbox.isChecked = registro.valorPremios
            binding.valorCreditoEditText.setText(registro.valorCredito ?: "")
            binding.contadoresCheckbox.isChecked = registro.contadoresVerificado

            // Si los contadores están verificados, cargar los valores MET
            if (registro.contadoresVerificado) {
                showContadoresMetFields()
                binding.coinInMetEditText.setText(registro.coinInMet ?: "")
                binding.coinOutMetEditText.setText(registro.coinOutMet ?: "")
                binding.jackpotMetEditText.setText(registro.jackpotMet ?: "")
            }

            // Cargar siempre los valores SCLM
            binding.coinInSclmEditText.setText(registro.coinInSclm ?: "")
            binding.coinOutSclmEditText.setText(registro.coinOutSclm ?: "")
            binding.jackpotSclmEditText.setText(registro.jackpotSclm ?: "")

            binding.observacionesEditText.setText(registro.observaciones ?: "")
            // Cargar estado
            binding.estadoSpinner.setText(EstadoInventarioEnum.toString(registro.estado), false)

            // Aplicar visibilidad según estado
            if (registro.estado == EstadoInventarioEnum.NO_ENCONTRADO) {
                hideAllFieldsExceptObservaciones()
            }
        } ?: run {
            // Si no hay registro (es nuevo), establecer "Operando" por defecto solo si el spinner está vacío
            if (binding.estadoSpinner.text.isNullOrEmpty()) {
                binding.estadoSpinner.setText("Operando", false)
            }
        }

        // Si se guardó exitosamente, navegar de vuelta
        if (uiState.guardadoExitoso) {
            Snackbar.make(binding.root, "Inventario guardado exitosamente", Snackbar.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }

        // Mostrar errores
        uiState.errorMessage?.let { errorMessage ->
            Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun guardarInventario() {
        val estadoSeleccionado = binding.estadoSpinner.text.toString()
        val estado = EstadoInventarioEnum.fromString(estadoSeleccionado)

        // Validar que se haya seleccionado un estado
        if (estadoSeleccionado.isEmpty()) {
            Snackbar.make(binding.root, "Debe seleccionar un estado", Snackbar.LENGTH_LONG).show()
            return
        }

        // Si el estado es NO_ENCONTRADO, solo guardar observaciones
        if (estado == EstadoInventarioEnum.NO_ENCONTRADO) {
            val observaciones = binding.observacionesEditText.text?.toString()

            if (observaciones.isNullOrBlank()) {
                Snackbar.make(binding.root, "Debe ingresar una observación cuando la máquina no está encontrada", Snackbar.LENGTH_LONG).show()
                return
            }

            viewModel.guardarInventario(
                codigoApuestaDiferente = false,
                codigoApuestaDiferenteValor = null,
                serialVerificado = false,
                serialDiferente = null,
                descripcionJuego = false,
                planPremios = false,
                valorPremios = false,
                valorCredito = null,
                contadoresVerificado = false,
                coinInMet = null,
                coinOutMet = null,
                jackpotMet = null,
                coinInSclm = null,
                coinOutSclm = null,
                jackpotSclm = null,
                observaciones = observaciones,
                estado = estadoSeleccionado
            )
            return
        }

        // Si el estado NO es NO_ENCONTRADO, validar y guardar todos los campos
        // Recopilar datos del formulario
        val codigoApuestaDiferente = binding.codigoApuestaDiferenteCheckbox.isChecked
        val codigoApuestaDiferenteValor = if (codigoApuestaDiferente)
            binding.codigoApuestaDiferenteEditText.text?.toString()?.trim()
        else
            null
        val serialVerificado = binding.serialVerificadoCheckbox.isChecked
        val serialDiferente = if (!serialVerificado) {
            binding.serialDiferenteEditText.text?.toString()?.trim()
        } else {
            null
        }
        val descripcionJuego = binding.descripcionJuegoCheckbox.isChecked
        val planPremios = binding.planPremiosCheckbox.isChecked
        val valorPremios = binding.valorPremiosCheckbox.isChecked
        val valorCredito = binding.valorCreditoEditText.text?.toString()?.trim()
        val contadoresVerificado = binding.contadoresCheckbox.isChecked

        // Datos de contadores MET (solo si están verificados)
        val coinInMet = if (contadoresVerificado) binding.coinInMetEditText.text?.toString()?.trim() else null
        val coinOutMet = if (contadoresVerificado) binding.coinOutMetEditText.text?.toString()?.trim() else null
        val jackpotMet = if (contadoresVerificado) binding.jackpotMetEditText.text?.toString()?.trim() else null

        // Datos de contadores SCLM (siempre se piden)
        val coinInSclm = binding.coinInSclmEditText.text?.toString()?.trim()
        val coinOutSclm = binding.coinOutSclmEditText.text?.toString()?.trim()
        val jackpotSclm = binding.jackpotSclmEditText.text?.toString()?.trim()

        val observaciones = binding.observacionesEditText.text?.toString()?.trim()

        // Guardar en el ViewModel
        viewModel.guardarInventario(
            codigoApuestaDiferente = codigoApuestaDiferente,
            codigoApuestaDiferenteValor = codigoApuestaDiferenteValor,
            serialVerificado = serialVerificado,
            serialDiferente = serialDiferente,
            descripcionJuego = descripcionJuego,
            planPremios = planPremios,
            valorPremios = valorPremios,
            valorCredito = valorCredito,
            contadoresVerificado = contadoresVerificado,
            coinInMet = coinInMet,
            coinOutMet = coinOutMet,
            jackpotMet = jackpotMet,
            coinInSclm = coinInSclm,
            coinOutSclm = coinOutSclm,
            jackpotSclm = jackpotSclm,
            observaciones = observaciones,
            estado = estadoSeleccionado
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}