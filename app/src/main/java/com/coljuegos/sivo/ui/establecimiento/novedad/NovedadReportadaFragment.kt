package com.coljuegos.sivo.ui.establecimiento.novedad

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
import com.coljuegos.sivo.databinding.FragmentNovedadReportadaBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.UUID

@AndroidEntryPoint
class NovedadReportadaFragment : Fragment() {

    private var _binding: FragmentNovedadReportadaBinding? = null

    private val binding get() = _binding!!

    private val args: NovedadReportadaFragmentArgs by navArgs()

    private val viewModel: NovedadReportadaViewModel by viewModels()

    private lateinit var novedadRegistradaAdapter: NovedadRegistradaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("NovedadFragment", "Registrando listener")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNovedadReportadaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupButtons()
        observeViewModel()

        // Cargar novedades registradas
        viewModel.loadNovedadesRegistradas(args.actaUuid)
    }

    override fun onResume() {
        super.onResume()
        // Recargar novedades al volver de registrar
        viewModel.loadNovedadesRegistradas(args.actaUuid)

        // Registrar listener para el botón de cámara del toolbar
        parentFragmentManager.setFragmentResultListener("camera_action", viewLifecycleOwner) { _, _ ->
            Log.d("NovedadFragment", "Recibido evento de cámara")
            navigateToGallery()
        }
    }

    private fun navigateToGallery() {
        val action = NovedadReportadaFragmentDirections
            .actionNovedadFragmentToGalleryFragment(
                actaUuid = args.actaUuid,
                fragmentOrigen = "novedad"
            )
        findNavController().navigate(action)
    }

    override fun onPause() {
        super.onPause()
        // Limpiar listener cuando el fragment no es visible
        parentFragmentManager.clearFragmentResultListener("camera_action")
    }

    private fun setupRecyclerView() {
        novedadRegistradaAdapter = NovedadRegistradaAdapter(
            onEditClick = { novedadConRegistro ->
                // Navegar directamente a RegistrarNovedadFragment en modo edición
                novedadConRegistro.novedad?.let { novedad ->
                    val action = NovedadReportadaFragmentDirections
                        .actionNovedadFragmentToRegistrarNovedadFragment(
                            actaUuid = args.actaUuid,
                            novedadRegistradaUuid = novedad.uuidNovedadRegistrada
                        )
                    findNavController().navigate(action)
                }
            },
            onDeleteClick = { novedadConRegistro ->
                // Mostrar diálogo de confirmación
                novedadConRegistro.novedad?.let { novedad ->
                    showDeleteConfirmationDialog(novedad.uuidNovedadRegistrada)
                }
            }
        )

        binding.recyclerNovedadesRegistradas.adapter = novedadRegistradaAdapter
    }

    private fun setupButtons() {
        binding.btnAgregar.setOnClickListener {
            val action = NovedadReportadaFragmentDirections
                .actionNovedadFragmentToRegistrarNovedadFragment(
                    actaUuid = args.actaUuid,
                    novedadRegistradaUuid = null
                )
            findNavController().navigate(action)
        }

        binding.btnAnterior.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSiguiente.setOnClickListener {
            // Navegar al siguiente fragment (Galería)
            val action = NovedadReportadaFragmentDirections
                .actionNovedadFragmentToResumenInventarioFragment(
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

    private fun updateUI(uiState: NovedadReportadaUiState) {
        // Mostrar/ocultar progress bar si lo tienes en el layout
        // binding.progressIndicator.isVisible = uiState.isLoading

        // Actualizar RecyclerView con novedades registradas
        novedadRegistradaAdapter.submitList(uiState.novedadesRegistradas)

        // Mostrar/ocultar mensaje de vacío
        binding.tvNovedadesRegistradas.isVisible = uiState.novedadesRegistradas.isNotEmpty()
        binding.recyclerNovedadesRegistradas.isVisible = uiState.novedadesRegistradas.isNotEmpty()

        // Mostrar errores
        uiState.errorMessage?.let { errorMessage ->
            showError(errorMessage)
            viewModel.clearError()
        }
    }

    private fun showDeleteConfirmationDialog(novedadRegistradaUuid: UUID) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar novedad")
            .setMessage("¿Está seguro que desea eliminar esta novedad registrada?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteNovedad(novedadRegistradaUuid)
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