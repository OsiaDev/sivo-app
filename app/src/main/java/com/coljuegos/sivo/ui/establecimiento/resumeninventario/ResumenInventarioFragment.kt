package com.coljuegos.sivo.ui.establecimiento.resumeninventario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.coljuegos.sivo.databinding.FragmentResumenInventarioBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class ResumenInventarioFragment : Fragment() {

    private var _binding: FragmentResumenInventarioBinding? = null

    private val binding get() = _binding!!

    private val args: ResumenInventarioFragmentArgs by navArgs()

    private val viewModel: ResumenInventarioViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResumenInventarioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        parentFragmentManager.setFragmentResultListener("camera_action", viewLifecycleOwner) { _, _ ->
            navigateToGallery()
        }
    }

    override fun onPause() {
        super.onPause()
        parentFragmentManager.clearFragmentResultListener("camera_action")
    }

    private fun navigateToGallery() {
        val action = ResumenInventarioFragmentDirections
            .actionResumenInventarioFragmentToGalleryFragment(args.actaUuid, "resumen_inventario")
        findNavController().navigate(action)
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUI(state)
                }
            }
        }
    }

    private fun updateUI(state: ResumenInventarioUiState) {
        // Mostrar/ocultar loading
        binding.progressBar.isVisible = state.isLoading

        // Actualizar estadísticas
        binding.tvInventariosOperando.text = state.inventariosOperando.toString()
        binding.tvInventariosApagados.text = state.inventariosApagados.toString()
        binding.tvInventariosNoEncontrados.text = state.inventariosNoEncontrados.toString()
        binding.tvNovedadesSinPlaca.text = state.novedadesSinPlaca.toString()
        binding.tvNovedadesConPlaca.text = state.novedadesConPlaca.toString()
        binding.tvTotalInventariosEncontrados.text = state.totalInventariosEncontrados.toString()
        binding.tvCodigoApuestaDiferente.text = state.codigoApuestaDiferente.toString()
        binding.tvInventariosSinDescripcionJuego.text = state.inventariosSinDescripcionJuego.toString()
        binding.tvInventariosSinSerial.text = state.inventariosSinSerial.toString()
        binding.tvInventariosSinPlanPremios.text = state.inventariosSinPlanPremios.toString()
        binding.tvInventariosSinValorPremios.text = state.inventariosSinValorPremios.toString()

        // Mostrar error si existe
        state.errorMessage?.let { errorMsg ->
            Snackbar.make(binding.root, errorMsg, Snackbar.LENGTH_LONG).show()
            viewModel.clearError()
        }

        if (binding.observacionesEditText.text.toString() != state.notas) {
            binding.observacionesEditText.setText(state.notas)
        }

        if (binding.observacionesOperadorEditText.text.toString() != state.observacionesOperador) {
            binding.observacionesOperadorEditText.setText(state.observacionesOperador)
        }
    }

    private fun setupListeners() {
        val scrollTouchListener = android.view.View.OnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            if ((event.action and android.view.MotionEvent.ACTION_MASK) == android.view.MotionEvent.ACTION_UP) {
                v.parent.requestDisallowInterceptTouchEvent(false)
            }
            false
        }
        binding.observacionesEditText.setOnTouchListener(scrollTouchListener)
        binding.observacionesOperadorEditText.setOnTouchListener(scrollTouchListener)

        binding.observacionesEditText.doOnTextChanged { text, _, _, _ ->
            if (binding.observacionesEditText.hasFocus()) {
                viewModel.guardarNotas(text?.toString() ?: "")
            }
        }
        binding.observacionesOperadorEditText.doOnTextChanged { text, _, _, _ ->
            if (binding.observacionesOperadorEditText.hasFocus()) {
                viewModel.guardarObservacionesOperador(text?.toString() ?: "")
            }
        }
        binding.btnLimpiarObservacion.setOnClickListener {
            com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirmación")
                .setMessage("¿Desea limpiar la observación de Coljuegos?")
                .setPositiveButton("Sí") { _, _ ->
                    binding.observacionesEditText.setText("")
                    viewModel.guardarNotas("")
                }
                .setNegativeButton("No", null)
                .show()
        }
        binding.btnSugeridaObservacion.setOnClickListener {
            com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirmación")
                .setMessage("¿Desea generar la observación sugerida basada en los datos recolectados?")
                .setPositiveButton("Sí") { _, _ ->
                    viewModel.generarObservacionSugerida()
                }
                .setNegativeButton("No", null)
                .show()
        }
        binding.btnAnterior.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSiguiente.setOnClickListener {
            val action = ResumenInventarioFragmentDirections
                .actionResumenInventarioFragmentToFirmaActaFragment(args.actaUuid)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}