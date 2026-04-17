package com.coljuegos.sivo.utils

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.coljuegos.sivo.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraHelper(
    private val fragment: Fragment,
    private val onImageCaptured: (Uri) -> Unit
) {

    private var imageUri: Uri? = null

    private val cameraPermissionLauncher = fragment.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            showError(fragment.getString(R.string.camera_permission_required))
        }
    }

    private val cameraLauncher = fragment.registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && imageUri != null) {
            onImageCaptured(imageUri!!)
        }
    }

    fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(
                fragment.requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            createImageUriMediaStore()
        } else {
            val photoFile = createImageFileLegacy()
            FileProvider.getUriForFile(
                fragment.requireContext(),
                "${fragment.requireContext().packageName}.fileprovider",
                photoFile
            )
        }
        cameraLauncher.launch(imageUri)
    }

    /**
     * API 29+ (Android 10+): Inserta la imagen directamente en MediaStore.
     * Esto hace que la foto quede visible en la galería del sistema de forma inmediata,
     * dentro del álbum "SIVO" en la carpeta Pictures.
     */
    private fun createImageUriMediaStore(): Uri? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "SIVO_${timeStamp}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/SIVO")
        }
        return fragment.requireContext().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )
    }

    /**
     * API < 29 (Android 9 o menor): Guarda en directorio público Pictures/SIVO.
     * El sistema de medios lo escaneará y aparecerá en la galería.
     */
    private fun createImageFileLegacy(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val sivoDir = File(picturesDir, "SGCT").apply { if (!exists()) mkdirs() }
        return File(sivoDir, "SGCT_${timeStamp}.jpg")
    }

    private fun showError(message: String) {
        MaterialAlertDialogBuilder(fragment.requireContext())
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

}