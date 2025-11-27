package com.coljuegos.sivo.ui.establecimiento.inventario

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.coljuegos.sivo.R
import com.coljuegos.sivo.databinding.FragmentInventarioReportadoBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.UUID

@AndroidEntryPoint
class InventarioReportadoFragment : Fragment() {

    private var _binding: FragmentInventarioReportadoBinding? = null
    private val binding get() = _binding!!

    private val args: InventarioReportadoFragmentArgs by navArgs()
    private val viewModel: InventarioReportadoViewModel by viewModels()

    private lateinit var inventarioRegistradoAdapter: InventarioRegistradoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("InventarioFragment", "Registrando listener")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventarioReportadoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupButtons()
        observeViewModel()

        // Cargar inventarios registrados
        viewModel.loadInventariosRegistrados(args.actaUuid)
    }

    override fun onResume() {
        super.onResume()
        // Recargar inventarios al volver de registrar
        viewModel.loadInventariosRegistrados(args.actaUuid)

        // Registrar listener para el botón de cámara del toolbar
        parentFragmentManager.setFragmentResultListener("camera_action", viewLifecycleOwner) { _, _ ->
            Log.d("InventarioFragment", "Recibido evento de cámara")
            navigateToGallery()
        }
    }

    private fun navigateToGallery() {
        val action = InventarioReportadoFragmentDirections
            .actionInventarioFragmentToGalleryFragment(
                actaUuid = args.actaUuid,
                fragmentOrigen = "inventario"
            )
        findNavController().navigate(action)
    }

    override fun onPause() {
        super.onPause()
        // Limpiar listener cuando el fragment no es visible
        parentFragmentManager.clearFragmentResultListener("camera_action")
    }

    private fun setupRecyclerView() {
        inventarioRegistradoAdapter = InventarioRegistradoAdapter(
            onEditClick = { inventarioConRegistro ->
                // Navegar directamente a RegistrarInventarioFragment en modo edición
                inventarioConRegistro.registro?.let { registro ->
                    val action = InventarioReportadoFragmentDirections
                        .actionInventarioFragmentToRegistrarInventarioFragment(
                            actaUuid = args.actaUuid,
                            inventarioUuid = inventarioConRegistro.inventario.uuidInventario,
                            inventarioRegistradoUuid = registro.uuidInventarioRegistrado
                        )
                    findNavController().navigate(action)
                }
            },
            onDeleteClick = { inventarioConRegistro ->
                // Mostrar diálogo de confirmación
                inventarioConRegistro.registro?.let { registro ->
                    showDeleteConfirmationDialog(registro.uuidInventarioRegistrado)
                }
            }
        )

        binding.recyclerInventariosRegistrados.adapter = inventarioRegistradoAdapter
    }

    private fun setupButtons() {
        // Botón Agregar - navega a InventarioActaFragment
        binding.btnAgregar.setOnClickListener {
            val action = InventarioReportadoFragmentDirections
                .actionInventarioFragmentToInventarioActaFragment(args.actaUuid)
            findNavController().navigate(action)
        }

        binding.btnAnterior.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSiguiente.setOnClickListener {
            // Navegar al siguiente fragment (Galería)
            val action = InventarioReportadoFragmentDirections
                .actionInventarioFragmentToNovedadFragment(
                    actaUuid = args.actaUuid
                )
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

    private fun updateUI(uiState: InventarioReportadoUiState) {
        // Mostrar/ocultar progress bar si lo tienes en el layout
        // binding.progressIndicator.isVisible = uiState.isLoading

        // Actualizar RecyclerView con inventarios registrados
        inventarioRegistradoAdapter.submitList(uiState.inventariosRegistrados)

        // Mostrar/ocultar mensaje de vacío
        binding.tvInventariosRegistrados.isVisible = uiState.inventariosRegistrados.isNotEmpty()
        binding.recyclerInventariosRegistrados.isVisible = uiState.inventariosRegistrados.isNotEmpty()

        // Mostrar errores
        uiState.errorMessage?.let { errorMessage ->
            showError(errorMessage)
            viewModel.clearError()
        }
    }

    private fun showDeleteConfirmationDialog(inventarioRegistradoUuid: UUID) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar inventario")
            .setMessage("¿Está seguro que desea eliminar este inventario registrado?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.eliminarInventarioRegistrado(inventarioRegistradoUuid)
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