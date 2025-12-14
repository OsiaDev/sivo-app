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

        setupSwipeRefresh()
        setupRecyclerView()
        observeViewModel()
        setupFragmentResultListener()
    }

    override fun onResume() {
        super.onResume()
        // Refrescar la lista cada vez que volvemos a este fragment
        viewModel.refreshActas()
    }

    private fun setupRecyclerView() {
        actaAdapter = ActaAdapter { acta ->
            val action = HomeFragmentDirections.actionHomeFragmentToActaVisitaFragment(acta.uuidActa)
            findNavController().navigate(action)
        }

        binding.recyclerViewLoans.apply {
            adapter = actaAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshActasFromBackend()
        }

        // Personalizar colores del indicador de refresh
        binding.swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )
    }

    /**
     * Escucha cuando se finaliza un acta para refrescar la lista
     */
    private fun setupFragmentResultListener() {
        parentFragmentManager.setFragmentResultListener(
            "request_refresh_actas",
            viewLifecycleOwner
        ) { _, bundle ->
            val shouldRefresh = bundle.getBoolean("should_refresh", false)
            if (shouldRefresh) {
                // Refrescar desde la base de datos local primero (rápido)
                viewModel.refreshActas()

                // Mostrar mensaje de confirmación
                Snackbar.make(
                    binding.root,
                    "Acta actualizada correctamente",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
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
        // Detener animación de refresh
        binding.swipeRefreshLayout.isRefreshing = uiState.isRefreshing

        // Actualizar nombre del usuario
        binding.tvFullName.text = uiState.userFullName ?: "Usuario"

        // Actualizar lista de actas
        actaAdapter.submitList(uiState.actas)

        // Mostrar mensaje de error si existe
        uiState.errorMessage?.let { errorMsg ->
            Snackbar.make(binding.root, errorMsg, Snackbar.LENGTH_LONG).show()
            viewModel.clearError()
        }

        // Mostrar mensaje de éxito si existe
        uiState.successMessage?.let { successMsg ->
            Snackbar.make(binding.root, successMsg, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}