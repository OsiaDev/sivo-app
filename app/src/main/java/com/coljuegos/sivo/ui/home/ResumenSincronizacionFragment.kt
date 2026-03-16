package com.coljuegos.sivo.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.coljuegos.sivo.R
import com.coljuegos.sivo.databinding.FragmentResumenSincronizacionBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ResumenSincronizacionFragment : Fragment() {

    private var _binding: FragmentResumenSincronizacionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ResumenSincronizacionViewModel by viewModels()
    private val args: ResumenSincronizacionFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResumenSincronizacionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.cargarDatos(args.actaUuid)

        setupCardListeners()
        observeViewModel()
    }

    private fun setupCardListeners() {
        binding.cardSincronizadas.setOnClickListener {
            navigateToGaleria("CARGADAS")
        }
        binding.cardPendientes.setOnClickListener {
            navigateToGaleria("PENDIENTES")
        }
        binding.cardErrores.setOnClickListener {
            navigateToGaleria("ERRORES")
        }
    }

    private fun navigateToGaleria(filtro: String) {
        val action = ResumenSincronizacionFragmentDirections
            .actionResumenSincronizacionFragmentToGaleriaImagenesFragment(
                args.actaUuid.toString(),
                filtro
            )
        findNavController().navigate(action)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.tvActaStatus.text = if (state.isActaSincronizada) "Sincronizada" else "Pendiente"
                binding.ivActaIcon.apply {
                    setImageResource(if (state.isActaSincronizada) R.drawable.ic_check_circle else R.drawable.ic_error_circle)
                    background = context.getDrawable(if (state.isActaSincronizada) R.drawable.bg_circle_green else R.drawable.bg_circle_red)
                    setPadding(6, 6, 6, 6)
                    imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE)
                }

                binding.tvSincronizadasCount.text = state.countSincronizadas.toString()
                binding.tvPendientesCount.text = state.countPendientes.toString()
                binding.tvErroresCount.text = state.countErrores.toString()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
