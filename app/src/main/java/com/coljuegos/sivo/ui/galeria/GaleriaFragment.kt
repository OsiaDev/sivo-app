package com.coljuegos.sivo.ui.galeria

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.coljuegos.sivo.R
import com.coljuegos.sivo.data.entity.ImagenEntity
import com.coljuegos.sivo.databinding.FragmentGaleriaBinding
import com.coljuegos.sivo.ui.base.BaseCameraFragment
import com.coljuegos.sivo.ui.imagen.ImagenViewModel
import com.coljuegos.sivo.utils.CameraHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class GaleriaFragment : BaseCameraFragment() {

    private var _binding: FragmentGaleriaBinding? = null

    private val binding get() = _binding!!

    private val args: GaleriaFragmentArgs by navArgs()

    private val imagenViewModel: ImagenViewModel by viewModels()

    private lateinit var galeriaAdapter: GaleriaAdapter

    private lateinit var cameraHelper: CameraHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializar CameraHelper en onCreate para registrar los ActivityResult correctamente
        cameraHelper = CameraHelper(this) { imageUri ->
            handleCapturedImage(imageUri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGaleriaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFab()
        observeViewModel()
        loadImagenes()
    }

    private fun setupRecyclerView() {
        galeriaAdapter = GaleriaAdapter(
            onImageClick = { imagen ->
                showImageFullScreen(imagen)
            },
            onDeleteClick = { imagen ->
                showDeleteConfirmationDialog(imagen)
            }
        )

        binding.recyclerViewImagenes.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = galeriaAdapter
        }

        // Actualizar título según fragment origen
        binding.textViewFragmentOrigen.text = when(args.fragmentOrigen) {
            "acta_visita" -> getString(R.string.galeria_fragment_acta_visita)
            "verificacion_contractual" -> getString(R.string.galeria_fragment_verificacion)
            "verificacion_siplaft" -> getString(R.string.galeria_fragment_siplaft)
            "inventario_reportado" -> getString(R.string.galeria_fragment_inventario)
            "foto_identificacion" -> getString(R.string.galeria_fragment_identificacion)
            else -> getString(R.string.galeria_titulo)
        }
    }

    private fun setupFab() {
        binding.fabCamera.setOnClickListener {
            // Usar el CameraHelper ya inicializado en onCreate
            cameraHelper.checkCameraPermissionAndOpen()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            imagenViewModel.imagenes.collect { imagenes ->
                galeriaAdapter.submitList(imagenes)

                // Mostrar mensaje si no hay imágenes
                if (imagenes.isEmpty()) {
                    binding.textViewNoImagenes.visibility = View.VISIBLE
                    binding.recyclerViewImagenes.visibility = View.GONE
                } else {
                    binding.textViewNoImagenes.visibility = View.GONE
                    binding.recyclerViewImagenes.visibility = View.VISIBLE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            imagenViewModel.errorMessage.collect { error ->
                error?.let {
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG)
                        .setAction("Reintentar") {
                            loadImagenes()
                        }
                        .show()
                    imagenViewModel.clearErrorMessage()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            imagenViewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    private fun loadImagenes() {
        imagenViewModel.loadImagenesByActaAndFragment(args.actaUuid, args.fragmentOrigen)
    }

    private fun showImageFullScreen(imagen: ImagenEntity) {
        Snackbar.make(binding.root, "Vista completa próximamente", Snackbar.LENGTH_SHORT).show()
    }

    private fun showDeleteConfirmationDialog(imagen: ImagenEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar imagen")
            .setMessage("¿Estás seguro de que deseas eliminar esta imagen?")
            .setPositiveButton("Eliminar") { _, _ ->
                imagenViewModel.deleteImagen(imagen)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun handleCapturedImage(imageUri: Uri) {
        // Copiar imagen a directorio interno de la app
        val fileName = "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg"
        val internalDir = File(requireContext().filesDir, "images").apply {
            if (!exists()) mkdirs()
        }
        val internalFile = File(internalDir, fileName)

        try {
            requireContext().contentResolver.openInputStream(imageUri)?.use { input ->
                internalFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // Guardar referencia en base de datos
            imagenViewModel.saveImagen(args.actaUuid, args.fragmentOrigen, internalFile.absolutePath, fileName)

            Snackbar.make(binding.root, "Imagen guardada exitosamente", Snackbar.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Snackbar.make(binding.root, "Error al guardar imagen: ${e.message}", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}