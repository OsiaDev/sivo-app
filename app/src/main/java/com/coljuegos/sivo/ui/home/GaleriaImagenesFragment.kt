package com.coljuegos.sivo.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.coljuegos.sivo.databinding.FragmentGaleriaImagenesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GaleriaImagenesFragment : Fragment() {

    private var _binding: FragmentGaleriaImagenesBinding? = null

    private val binding get() = _binding!!

    private val viewModel: GaleriaImagenesViewModel by viewModels()

    private val args: GaleriaImagenesFragmentArgs by navArgs()

    private val adapter = ImagenGaleriaAdapter { uuid ->
        viewModel.toggleSeleccion(uuid)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGaleriaImagenesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        viewModel.cargarImagenes(args.actaUuid, args.filtroEstado)
    }

    private fun setupRecyclerView() {
        binding.rvImagenes.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvImagenes.adapter = adapter
    }

    private fun setupListeners() {

        binding.btnSync.setOnClickListener {
            viewModel.iniciarSincronizacion()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                adapter.submitList(state.items)
                binding.btnSyncContainer.isVisible = state.canSync
                if (state.canSync) {
                    val selectedCount = state.items.count { it.isSelected }
                    binding.btnSync.text = "Sincronizar Seleccionadas ($selectedCount)"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
