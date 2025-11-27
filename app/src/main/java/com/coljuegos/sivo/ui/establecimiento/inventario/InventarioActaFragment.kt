package com.coljuegos.sivo.ui.establecimiento.inventario

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
import com.coljuegos.sivo.databinding.FragmentInventarioActaBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class InventarioActaFragment : Fragment() {

    private var _binding: FragmentInventarioActaBinding? = null
    private val binding get() = _binding!!

    private val args: InventarioActaFragmentArgs by navArgs()
    private val viewModel: InventarioActaViewModel by viewModels()

    private lateinit var inventarioAdapter: InventarioActaAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventarioActaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        setupButtons()
        observeViewModel()

        // Cargar inventario del acta (solo no registrados)
        viewModel.loadInventariosNoRegistrados(args.actaUuid)
    }

    override fun onResume() {
        super.onResume()
        // Recargar inventarios al volver de registrar
        viewModel.loadInventariosNoRegistrados(args.actaUuid)
    }

    private fun setupRecyclerView() {
        inventarioAdapter = InventarioActaAdapter(
            onItemClick = { inventario ->
                // Navegar a RegistrarInventarioFragment
                val action = InventarioActaFragmentDirections
                    .actionInventarioActaFragmentToRegistrarInventarioFragment(
                        actaUuid = args.actaUuid,
                        inventarioUuid = inventario.uuidInventario,
                        inventarioRegistradoUuid = null
                    )
                findNavController().navigate(action)
            }
        )
        binding.inventarioRecyclerView.adapter = inventarioAdapter
    }

    private fun setupSearch() {
        binding.searchEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.filterInventario(text?.toString() ?: "")
        }
    }

    private fun setupButtons() {
        binding.btnCerrar.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                updateUI(uiState)
            }
        }
    }

    private fun updateUI(uiState: InventarioActaUiState) {
        // Actualizar contador en el título
        val tituloConContador = getString(R.string.inventario_acta_total, uiState.totalInventariosNoRegistrados)
        binding.countValue.text = tituloConContador

        // Mostrar/ocultar progress bar
        binding.progressBar.isVisible = uiState.isLoading

        // Actualizar RecyclerView
        val inventariosAMostrar = if (uiState.searchQuery.isNotEmpty()) {
            uiState.filteredInventarios
        } else {
            uiState.inventariosNoRegistrados
        }

        inventarioAdapter.submitList(inventariosAMostrar)

        // Mostrar/ocultar estado vacío
        binding.inventarioRecyclerView.isVisible = inventariosAMostrar.isNotEmpty() && !uiState.isLoading
        binding.textViewNoInventario.isVisible = inventariosAMostrar.isEmpty() && !uiState.isLoading

        // Mostrar errores
        uiState.errorMessage?.let { errorMessage ->
            showError(errorMessage)
            viewModel.clearError()
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}