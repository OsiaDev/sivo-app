package com.coljuegos.sivo.ui.establecimiento.firma

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.coljuegos.sivo.databinding.FragmentFirmaActaBinding
import com.coljuegos.sivo.ui.common.SignatureViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FirmaActaFragment : Fragment() {

    private var _binding: FragmentFirmaActaBinding? = null
    private val binding get() = _binding!!

    private val args: FirmaActaFragmentArgs by navArgs()
    private val viewModel: FirmaActaViewModel by viewModels()

    // ViewModel compartido para las firmas
    private val signatureViewModel: SignatureViewModel by activityViewModels()

    enum class SignatureType {
        PRINCIPAL, SECUNDARIO, OPERADOR
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirmaActaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTextWatchers()
        setupSignatureButtons()
        setupNavigationButtons()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()

        // Configurar listener para el resultado de SignatureFragment
        setFragmentResultListener("signature_request") { _, bundle ->
            val signatureSaved = bundle.getBoolean("signature_saved", false)
            val signatureTypeOrdinal = bundle.getInt("signature_type", SignatureType.PRINCIPAL.ordinal)
            val signatureType = SignatureType.values()[signatureTypeOrdinal]

            if (signatureSaved) {
                handleSignatureSaved(signatureType)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Remover el listener cuando el fragment no está visible
        setFragmentResultListener("signature_request") { _, _ -> }
    }

    private fun setupTextWatchers() {
        // Fiscalizador Principal
        binding.nombrePrincipalText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateNombreFiscalizadorPrincipal(text?.toString() ?: "")
        }

        binding.ccPrincipalText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateCcFiscalizadorPrincipal(text?.toString() ?: "")
        }

        binding.cargoPrincipalText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateCargoFiscalizadorPrincipal(text?.toString() ?: "")
        }

        // Fiscalizador Secundario
        binding.nombreSecundarioText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateNombreFiscalizadorSecundario(text?.toString() ?: "")
        }

        binding.ccSecundarioText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateCcFiscalizadorSecundario(text?.toString() ?: "")
        }

        binding.cargoSecundarioText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateCargoFiscalizadorSecundario(text?.toString() ?: "")
        }

        // Operador
        binding.nombreOperadorText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateNombreOperador(text?.toString() ?: "")
        }

        binding.ccOperadorText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateCcOperador(text?.toString() ?: "")
        }

        binding.cargoOperadorText.doOnTextChanged { text, _, _, _ ->
            viewModel.updateCargoOperador(text?.toString() ?: "")
        }
    }

    private fun setupSignatureButtons() {
        // Fiscalizador Principal - Agregar firma
        binding.btnAddSignaturePrincipal.setOnClickListener {
            // Cargar firma existente si hay una
            viewModel.uiState.value.firmaFiscalizadorPrincipal?.let { bitmap ->
                signatureViewModel.saveSignature(bitmap)
            }
            navigateToSignatureFragment(SignatureType.PRINCIPAL)
        }

        // Fiscalizador Principal - Editar firma
        binding.btnEditSignaturePrincipal.setOnClickListener {
            // Cargar firma existente
            viewModel.uiState.value.firmaFiscalizadorPrincipal?.let { bitmap ->
                signatureViewModel.saveSignature(bitmap)
            }
            navigateToSignatureFragment(SignatureType.PRINCIPAL)
        }

        // Fiscalizador Secundario - Agregar firma
        binding.btnAddSignatureSecundario.setOnClickListener {
            // Cargar firma existente si hay una
            viewModel.uiState.value.firmaFiscalizadorSecundario?.let { bitmap ->
                signatureViewModel.saveSignature(bitmap)
            }
            navigateToSignatureFragment(SignatureType.SECUNDARIO)
        }

        // Fiscalizador Secundario - Editar firma
        binding.btnEditSignatureSecundario.setOnClickListener {
            // Cargar firma existente
            viewModel.uiState.value.firmaFiscalizadorSecundario?.let { bitmap ->
                signatureViewModel.saveSignature(bitmap)
            }
            navigateToSignatureFragment(SignatureType.SECUNDARIO)
        }

        // Operador - Agregar firma
        binding.btnAddSignatureOperador.setOnClickListener {
            // Cargar firma existente si hay una
            viewModel.uiState.value.firmaOperador?.let { bitmap ->
                signatureViewModel.saveSignature(bitmap)
            }
            navigateToSignatureFragment(SignatureType.OPERADOR)
        }

        // Operador - Editar firma
        binding.btnEditSignatureOperador.setOnClickListener {
            // Cargar firma existente
            viewModel.uiState.value.firmaOperador?.let { bitmap ->
                signatureViewModel.saveSignature(bitmap)
            }
            navigateToSignatureFragment(SignatureType.OPERADOR)
        }
    }

    private fun setupNavigationButtons() {
        binding.btnAnterior.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSiguiente.setOnClickListener {
            viewModel.saveFirmaActa(
                onSuccess = {
                    Snackbar.make(
                        binding.root,
                        "Firmas guardadas exitosamente",
                        Snackbar.LENGTH_SHORT
                    ).show()

                    val action = FirmaActaFragmentDirections
                        .actionFirmaActaFragmentToResumenActaFragment(args.actaUuid)
                    findNavController().navigate(action)
                },
                onError = { message ->
                    Snackbar.make(
                        binding.root,
                        message,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            )
        }
    }

    private fun navigateToSignatureFragment(signatureType: SignatureType) {
        // Guardar el tipo de firma en el ViewModel compartido
        signatureViewModel.setSignatureType(signatureType.ordinal)

        val action = FirmaActaFragmentDirections.actionFirmaActaFragmentToSignatureFragment()
        findNavController().navigate(action)
    }

    private fun handleSignatureSaved(signatureType: SignatureType) {
        // Obtener la firma guardada del SignatureViewModel
        signatureViewModel.signatureBitmap.value?.let { bitmap ->
            // Guardar según el tipo de firma recibido
            when (signatureType) {
                SignatureType.PRINCIPAL -> {
                    viewModel.saveFirmaPrincipal(bitmap)
                    Snackbar.make(
                        binding.root,
                        "Firma del fiscalizador principal guardada",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                SignatureType.SECUNDARIO -> {
                    viewModel.saveFirmaSecundario(bitmap)
                    Snackbar.make(
                        binding.root,
                        "Firma del fiscalizador acompañante guardada",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                SignatureType.OPERADOR -> {
                    viewModel.saveFirmaOperador(bitmap)
                    Snackbar.make(
                        binding.root,
                        "Firma del operador guardada",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }

            // Limpiar la firma del SignatureViewModel después de transferirla
            signatureViewModel.clearSignature()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: FirmaActaUiState) {
        // Los campos de texto (nombre, cc, cargo) se actualizan automáticamente con doOnTextChanged
        // pero si cargas datos existentes, necesitas actualizar los EditTexts

        // ==========================================
        // FISCALIZADOR PRINCIPAL
        // ==========================================

        if (binding.nombrePrincipalText.text.toString() != state.nombreFiscalizadorPrincipal) {
            binding.nombrePrincipalText.setText(state.nombreFiscalizadorPrincipal)
        }
        if (binding.ccPrincipalText.text.toString() != state.ccFiscalizadorPrincipal) {
            binding.ccPrincipalText.setText(state.ccFiscalizadorPrincipal)
        }
        if (binding.cargoPrincipalText.text.toString() != state.cargoFiscalizadorPrincipal) {
            binding.cargoPrincipalText.setText(state.cargoFiscalizadorPrincipal)
        }

        // Mostrar/ocultar miniatura de firma principal
        if (state.firmaFiscalizadorPrincipal != null) {
            binding.layoutSignaturePrincipalPreview.isVisible = true
            binding.btnAddSignaturePrincipal.isVisible = false
            binding.ivSignaturePrincipal.setImageBitmap(state.firmaFiscalizadorPrincipal)
        } else {
            binding.layoutSignaturePrincipalPreview.isVisible = false
            binding.btnAddSignaturePrincipal.isVisible = true
        }

        // ==========================================
        // FISCALIZADOR SECUNDARIO
        // ==========================================

        if (binding.nombreSecundarioText.text.toString() != state.nombreFiscalizadorSecundario) {
            binding.nombreSecundarioText.setText(state.nombreFiscalizadorSecundario)
        }
        if (binding.ccSecundarioText.text.toString() != state.ccFiscalizadorSecundario) {
            binding.ccSecundarioText.setText(state.ccFiscalizadorSecundario)
        }
        if (binding.cargoSecundarioText.text.toString() != state.cargoFiscalizadorSecundario) {
            binding.cargoSecundarioText.setText(state.cargoFiscalizadorSecundario)
        }

        // Mostrar/ocultar miniatura de firma secundario
        if (state.firmaFiscalizadorSecundario != null) {
            binding.layoutSignatureSecundarioPreview.isVisible = true
            binding.btnAddSignatureSecundario.isVisible = false
            binding.ivSignatureSecundario.setImageBitmap(state.firmaFiscalizadorSecundario)
        } else {
            binding.layoutSignatureSecundarioPreview.isVisible = false
            binding.btnAddSignatureSecundario.isVisible = true
        }

        // ==========================================
        // OPERADOR
        // ==========================================

        if (binding.nombreOperadorText.text.toString() != state.nombreOperador) {
            binding.nombreOperadorText.setText(state.nombreOperador)
        }
        if (binding.ccOperadorText.text.toString() != state.ccOperador) {
            binding.ccOperadorText.setText(state.ccOperador)
        }
        if (binding.cargoOperadorText.text.toString() != state.cargoOperador) {
            binding.cargoOperadorText.setText(state.cargoOperador)
        }

        // Mostrar/ocultar miniatura de firma operador
        if (state.firmaOperador != null) {
            binding.layoutSignatureOperadorPreview.isVisible = true
            binding.btnAddSignatureOperador.isVisible = false
            binding.ivSignatureOperador.setImageBitmap(state.firmaOperador)
        } else {
            binding.layoutSignatureOperadorPreview.isVisible = false
            binding.btnAddSignatureOperador.isVisible = true
        }

        // Mostrar error si existe
        state.errorMessage?.let { message ->
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}