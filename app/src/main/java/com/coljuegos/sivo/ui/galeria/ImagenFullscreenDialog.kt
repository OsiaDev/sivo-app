package com.coljuegos.sivo.ui.galeria

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.coljuegos.sivo.R
import com.coljuegos.sivo.data.entity.ImagenEntity
import com.coljuegos.sivo.databinding.DialogImagenFullscreenBinding
import com.coljuegos.sivo.di.Extenxion.toReadableFileSize
import java.io.File
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class ImagenFullscreenDialog : DialogFragment() {

    private var _binding: DialogImagenFullscreenBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_RUTA = "ruta_imagen"
        private const val ARG_NOMBRE = "nombre_imagen"
        private const val ARG_FECHA = "fecha_imagen"
        private const val ARG_TAMANO = "tamano_imagen"

        fun newInstance(imagen: ImagenEntity): ImagenFullscreenDialog {
            val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            return ImagenFullscreenDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_RUTA, imagen.rutaImagen)
                    putString(ARG_NOMBRE, imagen.nombreImagen)
                    putString(ARG_FECHA, formatter.format(imagen.fechaCaptura))
                    putLong(ARG_TAMANO, imagen.tamanoBytesImagen)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawableResource(android.R.color.black)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogImagenFullscreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ruta = arguments?.getString(ARG_RUTA) ?: return
        val nombre = arguments?.getString(ARG_NOMBRE) ?: ""
        val fecha = arguments?.getString(ARG_FECHA) ?: ""
        val tamano = arguments?.getLong(ARG_TAMANO) ?: 0L

        // Cargar imagen en PhotoView con Glide
        val file = File(ruta)
        if (file.exists()) {
            Glide.with(this)
                .load(file)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .error(R.drawable.ic_camara)
                .into(binding.photoView)
        } else {
            binding.photoView.setImageResource(R.drawable.ic_camara)
        }

        binding.tvNombreImagen.text = nombre
        binding.tvFechaImagen.text = fecha
        binding.tvTamanoImagen.text = tamano.toReadableFileSize()

        binding.btnCerrar.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}