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
import com.coljuegos.sivo.R
import com.coljuegos.sivo.databinding.FragmentInventarioBingoReportadoBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InventarioBingoReportadoFragment : Fragment() {

    private var _binding: FragmentInventarioBingoReportadoBinding? = null
    private val binding get() = _binding!!

    private val args: InventarioBingoReportadoFragmentArgs by navArgs()
    private val viewModel: InventarioBingoReportadoViewModel by viewModels()

    private lateinit var adapter: InventarioBingoRegistradoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventarioBingoReportadoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        setupButtons()
        observeViewModel()
        viewModel.loadInventarios(args.actaUuid)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadInventarios(args.actaUuid)
    }

    private fun setupRecyclerView() {
        adapter = InventarioBingoRegistradoAdapter(
            onItemClick = { item ->
                // Si no tiene registro, navegar a registrar
                val action = InventarioBingoReportadoFragmentDirections
                    .actionInventarioBingoReportadoFragmentToRegistrarBingoFragment(
                        actaUuid = args.actaUuid,
                        inventarioUuid = item.inventario.uuidInventario,
                        inventarioBingoRegistradoUuid = item.registro?.uuidInventarioBingoRegistrado
                    )
                findNavController().navigate(action)
            },
            onEditClick = { item ->
                item.registro?.let {
                    val action = InventarioBingoReportadoFragmentDirections
                        .actionInventarioBingoReportadoFragmentToRegistrarBingoFragment(
                            actaUuid = args.actaUuid,
                            inventarioUuid = item.inventario.uuidInventario,
                            inventarioBingoRegistradoUuid = it.uuidInventarioBingoRegistrado
                        )
                    findNavController().navigate(action)
                }
            },
            onDeleteClick = { item ->
                item.registro?.let { reg ->
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.confirmar_eliminar_title)
                        .setMessage(R.string.confirmar_eliminar_message)
                        .setPositiveButton(R.string.eliminar) { _, _ ->
                            viewModel.deleteRegistro(reg.uuidInventarioBingoRegistrado, args.actaUuid)
                        }
                        .setNegativeButton(R.string.cancelar, null)
                        .show()
                }
            }
        )
        binding.recyclerBingos.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.filterInventario(text?.toString())
        }
    }

    private fun setupButtons() {
        binding.btnAnterior.setOnClickListener { findNavController().navigateUp() }
        binding.btnSiguiente.setOnClickListener {
            val action = InventarioBingoReportadoFragmentDirections
                .actionInventarioBingoReportadoFragmentToNovedadFragment(args.actaUuid)
            findNavController().navigate(action)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.progressIndicator.isVisible = state.isLoading
                binding.emptyText.isVisible = !state.isLoading && state.filteredInventarios.isEmpty()
                binding.recyclerBingos.isVisible = !state.isLoading && state.filteredInventarios.isNotEmpty()
                binding.totalText.text = getString(R.string.inventario_bingo_total, state.totalRegistrados, state.inventarios.size)
                adapter.submitList(state.filteredInventarios)
                state.errorMessage?.let {
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}