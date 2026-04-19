package com.coljuegos.sivo.ui.establecimiento.bingo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.coljuegos.sivo.databinding.FragmentInventarioBingoActaBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InventarioBingoActaFragment : Fragment() {

    private var _binding: FragmentInventarioBingoActaBinding? = null
    private val binding get() = _binding!!

    private val args: InventarioBingoActaFragmentArgs by navArgs()
    private val viewModel: InventarioBingoActaViewModel by viewModels()

    private lateinit var adapter: InventarioBingoActaAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventarioBingoActaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        observeViewModel()
        viewModel.loadNoRegistrados(args.actaUuid)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadNoRegistrados(args.actaUuid)
    }

    private fun setupRecyclerView() {
        adapter = InventarioBingoActaAdapter(
            onItemClick = { inventario ->
                val action = InventarioBingoActaFragmentDirections
                    .actionInventarioBingoActaFragmentToRegistrarBingoFragment(
                        actaUuid = args.actaUuid,
                        inventarioUuid = inventario.uuidInventario,
                        inventarioBingoRegistradoUuid = null
                    )
                findNavController().navigate(action)
            }
        )
        binding.recyclerBingosActa.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.filterInventario(text?.toString())
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.progressIndicator.isVisible = state.isLoading
                binding.emptyText.isVisible = !state.isLoading && state.filteredInventarios.isEmpty()
                binding.recyclerBingosActa.isVisible = state.filteredInventarios.isNotEmpty()
                adapter.submitList(state.filteredInventarios)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}