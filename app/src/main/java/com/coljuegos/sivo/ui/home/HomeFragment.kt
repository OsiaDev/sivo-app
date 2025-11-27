package com.coljuegos.sivo.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.coljuegos.sivo.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var actaAdapter: ActaAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        actaAdapter = ActaAdapter { acta ->
            val action = HomeFragmentDirections.actionHomeFragmentToActaVisitaFragment(acta.uuidActa)
            findNavController().navigate(action)
        }

        binding.recyclerViewLoans.apply {
            adapter = actaAdapter
            layoutManager = LinearLayoutManager(requireContext())

            // Opcional: agregar separadores entre items si es necesario
            // addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                updateUI(uiState)
            }
        }
    }

    private fun updateUI(uiState: HomeUiState) {
        // Actualizar nombre del usuario
        binding.tvFullName.text = uiState.userFullName ?: "Usuario"

        // Actualizar lista de actas
        actaAdapter.submitList(uiState.actas)

        // Mostrar/ocultar loading
        // Si tienes un loading indicator, puedes manejarlo aquí
        // binding.progressBar.visibility = if (uiState.isLoading) View.VISIBLE else View.GONE

        // Mostrar errores
        uiState.errorMessage?.let { errorMessage ->
            showError(errorMessage)
            viewModel.clearError()
        }

        // Manejar estado vacío
        if (!uiState.isLoading && uiState.actas.isEmpty() && uiState.errorMessage == null) {
            showEmptyState()
        } else {
            hideEmptyState()
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Reintentar") {
                viewModel.refreshActas()
            }
            .show()
    }

    private fun showEmptyState() {
        // Aquí puedes mostrar un mensaje cuando no hay actas
        // Por ejemplo, cambiar la visibilidad de un TextView o mostrar una imagen
        binding.recyclerViewLoans.visibility = View.GONE
        // binding.emptyStateView.visibility = View.VISIBLE
    }

    private fun hideEmptyState() {
        binding.recyclerViewLoans.visibility = View.VISIBLE
        // binding.emptyStateView.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}