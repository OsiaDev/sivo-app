package com.coljuegos.sivo.ui.establecimiento.resumen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.coljuegos.sivo.R
import com.coljuegos.sivo.data.entity.ActaStateEnum
import com.coljuegos.sivo.databinding.FragmentResumenActaBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ResumenActaFragment : Fragment() {

    private var _binding: FragmentResumenActaBinding? = null

    private val binding get() = _binding!!

    private val viewModel: ResumenActaViewModel by viewModels()

    private val args: ResumenActaFragmentArgs by navArgs()

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var cancellationTokenSource: CancellationTokenSource? = null

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            obtenerUbicacion()
        } else {
            Snackbar.make(
                binding.root,
                R.string.resumen_acta_permiso_ubicacion_denegado,
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResumenActaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setupListeners()
        observeViewModel()

        // Cargar datos del acta
        viewModel.loadActa(args.actaUuid)

        // Solicitar ubicación
        solicitarUbicacion()
    }

    private fun setupListeners() {
        binding.btnAtras.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnFinalizar.setOnClickListener {
            mostrarDialogoConfirmacion()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                updateUI(uiState)
            }
        }
    }

    private fun updateUI(uiState: ResumenActaUiState) {
        // Progress bar
        binding.progressBar.isVisible = uiState.isLoading || uiState.isSincronizando

        // Información del acta
        uiState.acta?.let { acta ->
            binding.numActaText.text = acta.numActa.toString()
            binding.establecimientoText.text = acta.establecimientoActa
            binding.operadorText.text = acta.nombreOperadorActa
            binding.direccionText.text = acta.direccionActa
        }

        // Estado
        binding.estadoText.text = when (uiState.estadoActa) {
            ActaStateEnum.ACTIVE -> getString(R.string.resumen_acta_estado_activo)
            ActaStateEnum.COMPLETE -> getString(R.string.resumen_acta_estado_completo)
            ActaStateEnum.SINCRONIZADO -> getString(R.string.resumen_acta_estado_sincronizado)
            else -> uiState.estadoActa.name
        }

        // Ubicación
        if (uiState.ubicacionObtenida) {
            binding.ubicacionText.text = String.format(
                "%.6f, %.6f",
                uiState.latitud,
                uiState.longitud
            )
        } else {
            binding.ubicacionText.text = getString(R.string.resumen_acta_obteniendo_ubicacion)
        }

        // Deshabilitar botón finalizar si ya está completo o sincronizado
        binding.btnFinalizar.isEnabled = uiState.estadoActa == ActaStateEnum.ACTIVE &&
                !uiState.isLoading &&
                !uiState.isSincronizando

        // Mensajes
        uiState.errorMessage?.let { message ->
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            viewModel.clearMessages()
        }

        uiState.successMessage?.let { message ->
            Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }

        // Navegar solo cuando la bandera lo indique
        if (uiState.debeNavegarAlHome && !uiState.isLoading && !uiState.isSincronizando) {
            binding.root.postDelayed({
                navegarAHome()
            }, 1500)
        }
    }

    /**
     * Navega al HomeFragment y notifica que debe refrescar la lista de actas
     */
    private fun navegarAHome() {
        // Limpiar toda la pila de navegación hasta Home y notificar refresh
        parentFragmentManager.setFragmentResult(
            "request_refresh_actas",
            Bundle().apply {
                putBoolean("should_refresh", true)
            }
        )

        // Navegar a Home limpiando la pila
        findNavController().popBackStack(R.id.homeFragment, false)
    }

    private fun solicitarUbicacion() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                obtenerUbicacion()
            }

            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                obtenerUbicacion()
            }

            else -> {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun obtenerUbicacion() {
        try {
            cancellationTokenSource = CancellationTokenSource()

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource!!.token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.actualizarUbicacion(
                        latitud = location.latitude,
                        longitud = location.longitude
                    )
                } else {
                    // Si no se puede obtener ubicación actual, usar la última conocida
                    obtenerUltimaUbicacionConocida()
                }
            }.addOnFailureListener {
                // Si falla, intentar obtener la última ubicación conocida
                obtenerUltimaUbicacionConocida()
            }
        } catch (_: SecurityException) {
            Snackbar.make(
                binding.root,
                R.string.resumen_acta_permiso_ubicacion_denegado,
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun obtenerUltimaUbicacionConocida() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.actualizarUbicacion(
                        latitud = location.latitude,
                        longitud = location.longitude
                    )
                }
            }
        } catch (_: SecurityException) {
            // Ignorar si no tenemos permisos
        }
    }

    private fun mostrarDialogoConfirmacion() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Finalizar Acta")
            .setMessage("¿Está seguro que desea finalizar el acta? " +
                    "Esta acción marcará el acta como completa y se intentará sincronizar con el servidor.")
            .setPositiveButton("Finalizar") { _, _ ->
                finalizarActa()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun finalizarActa() {
        viewModel.finalizarActa(args.actaUuid)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cancellationTokenSource?.cancel()
        _binding = null
    }

}