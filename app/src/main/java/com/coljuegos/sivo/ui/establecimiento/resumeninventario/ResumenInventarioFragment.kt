package com.coljuegos.sivo.ui.establecimiento.resumeninventario

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
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

    }

    override fun onPause() {
        super.onPause()
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
        binding.tvInventariosOperandoApagado.text = state.inventariosOperandoApagado.toString()
        binding.tvInventariosNoEncontrados.text = state.inventariosNoEncontrados.toString()
        binding.tvNovedadesSinPlaca.text = state.novedadesSinPlaca.toString()
        binding.tvNovedadesConPlaca.text = state.novedadesConPlaca.toString()
        binding.tvTotalInventariosEncontrados.text = state.totalInventariosEncontrados.toString()
        binding.tvCodigoApuestaDiferente.text = state.codigoApuestaDiferente.toString()

        // Mostrar error si existe
        state.errorMessage?.let { errorMsg ->
            Snackbar.make(binding.root, errorMsg, Snackbar.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    private fun setupListeners() {
        binding.btnAnterior.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSiguiente.setOnClickListener {
            val action = ResumenInventarioFragmentDirections
                .actionResumenInventarioFragmentToResumenActaFragment(args.actaUuid)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}