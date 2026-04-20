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
import com.coljuegos.sivo.databinding.FragmentInventarioBingoActaBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
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
        setupButtons()
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
        binding.inventarioRecyclerView.adapter = adapter
    }

    private fun setupSearch() {
        binding.searchEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.filterInventario(text?.toString())
        }
    }

    private fun setupButtons() {
        binding.btnCerrar.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnNoEncontrados.setOnClickListener {
            val cantidad = viewModel.uiState.value.totalInventariosNoRegistrados
            if (cantidad == 0) {
                Snackbar.make(binding.root, "No hay inventarios pendientes por registrar", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Registrar como no encontrados")
                .setMessage("Se marcarán $cantidad inventario(s) de bingo como 'No encontrado'. ¿Deseas continuar?")
                .setPositiveButton("Confirmar") { _, _ ->
                    viewModel.registrarTodosComoNoEncontrados(args.actaUuid)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                binding.progressBar.isVisible = uiState.isLoading

                binding.countValue.text = getString(
                    R.string.inventario_bingo_acta_total,
                    uiState.totalInventariosNoRegistrados
                )

                val inventariosAMostrar = if (uiState.searchQuery.isNotEmpty()) {
                    uiState.filteredInventarios
                } else {
                    uiState.inventariosNoRegistrados
                }

                adapter.submitList(inventariosAMostrar)
                binding.inventarioRecyclerView.isVisible =
                    inventariosAMostrar.isNotEmpty() && !uiState.isLoading
                binding.textViewNoInventario.isVisible =
                    inventariosAMostrar.isEmpty() && !uiState.isLoading

                if (uiState.registroMasivoExitoso) {
                    Snackbar.make(binding.root, "Inventarios registrados como no encontrados", Snackbar.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }

                uiState.errorMessage?.let {
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