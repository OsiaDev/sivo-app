package com.coljuegos.sivo.ui.common

import android.app.AlertDialog
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.coljuegos.sivo.R
import com.coljuegos.sivo.databinding.FragmentSignatureBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignatureFragment : Fragment() {

    private var _binding: FragmentSignatureBinding? = null

    private val binding get() = _binding!!

    private val viewModel: SignatureViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignatureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackPressHandler()
        setupButtons()
        loadExistingSignature()
    }

    override fun onResume() {
        super.onResume()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    override fun onPause() {
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        super.onPause()
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleCancelAction()
                }
            }
        )
    }

    private fun setupButtons() {
        binding.btnGuardar.setOnClickListener {
            if (binding.signatureView.hasSignature()) {
                val bitmap = binding.signatureView.getSignatureBitmap()
                viewModel.saveSignature(bitmap)

                // Enviar resultado al fragment anterior con el tipo de firma
                setFragmentResult(
                    SIGNATURE_REQUEST_KEY,
                    Bundle().apply {
                        putBoolean(SIGNATURE_SAVED_KEY, true)
                        putInt("signature_type", viewModel.signatureType.value)
                    }
                )

                findNavController().navigateUp()
            } else {
                Snackbar.make(
                    binding.root,
                    "Debe realizar una firma antes de guardar",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        binding.btnReiniciar.setOnClickListener {
            if (binding.signatureView.hasSignature()) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Limpiar firma")
                    .setMessage("¿Está seguro que desea borrar la firma actual?")
                    .setPositiveButton("Sí") { _, _ ->
                        binding.signatureView.clear()
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        }

        binding.btnCancelar.setOnClickListener {
            handleCancelAction()
        }
    }

    private fun handleCancelAction() {
        if (binding.signatureView.hasSignature()) {
            AlertDialog.Builder(requireContext())
                .setTitle("Cancelar firma")
                .setMessage("¿Está seguro que desea salir sin guardar?")
                .setPositiveButton("Sí") { _, _ ->
                    findNavController().navigateUp()
                }
                .setNegativeButton("No", null)
                .show()
        } else {
            findNavController().navigateUp()
        }
    }

    private fun loadExistingSignature() {
        // Esperar a que el SignatureView tenga dimensiones antes de cargar la firma
        binding.signatureView.post {
            viewModel.signatureBitmap.value?.let { bitmap ->
                binding.signatureView.setSignatureBitmap(bitmap)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val SIGNATURE_REQUEST_KEY = "signature_request"
        const val SIGNATURE_SAVED_KEY = "signature_saved"
    }
    
}