package com.coljuegos.sivo.ui.maestros

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.coljuegos.sivo.R
import com.coljuegos.sivo.databinding.FragmentMaestrosSincronizacionBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class MaestrosSincronizacionFragment : Fragment() {

    private var _binding: FragmentMaestrosSincronizacionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MaestrosViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMaestrosSincronizacionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeViewModel()
        verificarEstado()
    }

    private fun setupListeners() {
        // Botón de sincronización normal
        binding.syncButton.setOnClickListener {
            viewModel.sincronizarMaestros()
        }

        // Botón de actualización forzada
        binding.forceUpdateButton.setOnClickListener {
            viewModel.forzarActualizacion()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.sincronizacionState.collect { state ->
                updateUI(state)
            }
        }
    }

    private fun verificarEstado() {
        viewLifecycleOwner.lifecycleScope.launch {
            val necesitaSincronizacion = viewModel.necesitaSincronizacion()
            val cantidadTipos = viewModel.getCantidadTiposApuesta()

            if (necesitaSincronizacion) {
                binding.statusValue.text = getString(R.string.maestros_estado_pendiente)
                binding.lastSyncValue.text = getString(R.string.maestros_nunca_sincronizado)
            } else {
                binding.statusValue.text = getString(
                    R.string.maestros_estado_sincronizado_con_cantidad,
                    cantidadTipos
                )
                // Aquí podrías obtener la fecha de última actualización desde SharedPreferences
                binding.lastSyncValue.text = obtenerFechaActual()
            }
        }
    }

    private fun updateUI(state: SincronizacionState) {
        when (state) {
            is SincronizacionState.Idle -> {
                showLoading(false)
                hideStatusMessage()
            }

            is SincronizacionState.Loading -> {
                showLoading(true)
                showStatusMessage(getString(R.string.maestros_sincronizando))
                binding.syncButton.isEnabled = false
                binding.forceUpdateButton.isEnabled = false
            }

            is SincronizacionState.Success -> {
                showLoading(false)
                hideStatusMessage()
                binding.syncButton.isEnabled = true
                binding.forceUpdateButton.isEnabled = true

                // Actualizar estado
                verificarEstado()

                // Mostrar mensaje de éxito
                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()

                // Resetear el estado
                viewModel.resetState()
            }

            is SincronizacionState.Error -> {
                showLoading(false)
                showStatusMessage(state.message, isError = true)
                binding.syncButton.isEnabled = true
                binding.forceUpdateButton.isEnabled = true

                // Mostrar error
                Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()

                // Resetear el estado
                viewModel.resetState()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressIndicator.isVisible = show
    }

    private fun showStatusMessage(message: String, isError: Boolean = false) {
        binding.statusMessage.isVisible = true
        binding.statusMessage.text = message

        if (isError) {
            binding.statusMessage.setTextColor(
                resources.getColor(R.color.red, null)
            )
        } else {
            binding.statusMessage.setTextColor(
                resources.getColor(R.color.blue, null)
            )
        }
    }

    private fun hideStatusMessage() {
        binding.statusMessage.isVisible = false
    }

    private fun obtenerFechaActual(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(Date())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}