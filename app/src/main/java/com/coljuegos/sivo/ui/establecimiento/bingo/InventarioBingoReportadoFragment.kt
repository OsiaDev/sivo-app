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
import com.coljuegos.sivo.databinding.FragmentInventarioBingoReportadoBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.UUID

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
        viewModel.loadRegistrados(args.actaUuid)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadRegistrados(args.actaUuid)
    }

    private fun setupRecyclerView() {
        adapter = InventarioBingoRegistradoAdapter(
            onEditClick = { item ->
                val action = InventarioBingoReportadoFragmentDirections
                    .actionInventarioBingoReportadoFragmentToRegistrarBingoFragment(
                        actaUuid = args.actaUuid,
                        inventarioUuid = item.inventario.uuidInventario,
                        inventarioBingoRegistradoUuid = item.registro.uuidInventarioBingoRegistrado
                    )
                findNavController().navigate(action)
            },
            onDeleteClick = { inventarioConRegistro ->
                // Mostrar diálogo de confirmación
                showDeleteConfirmationDialog(inventarioConRegistro.registro.uuidInventarioBingoRegistrado)
            }
        )
        binding.recyclerInventariosRegistrados.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.filterInventario(text?.toString())
        }
    }

    private fun setupButtons() {
        binding.btnAnterior.setOnClickListener { findNavController().navigateUp() }

        binding.btnAgregar.setOnClickListener {
            val action = InventarioBingoReportadoFragmentDirections
                .actionInventarioBingoReportadoFragmentToInventarioBingoActaFragment(args.actaUuid)
            findNavController().navigate(action)
        }

        binding.btnSiguiente.setOnClickListener {
            val action = InventarioBingoReportadoFragmentDirections
                .actionInventarioBingoReportadoFragmentToNovedadFragment(args.actaUuid)
            findNavController().navigate(action)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                updateUI(uiState)
            }
        }
    }

    private fun updateUI(uiState: InventarioBingoReportadoUiState) {
        val hayRegistros = uiState.filteredRegistrados.isNotEmpty()

        binding.searchInputLayout.isVisible = hayRegistros
        binding.tvInventariosRegistrados.isVisible = hayRegistros
        binding.recyclerInventariosRegistrados.isVisible = hayRegistros && !uiState.isLoading

        adapter.submitList(uiState.filteredRegistrados)

        uiState.errorMessage?.let {
            Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun showDeleteConfirmationDialog(inventarioRegistradoUuid: UUID) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Retirar revisión")
            .setMessage("¿Está seguro que desea retirar la revisión de este inventario?")
            .setPositiveButton("Retirar") { _, _ ->
                viewModel.deleteRegistro(inventarioRegistradoUuid)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}