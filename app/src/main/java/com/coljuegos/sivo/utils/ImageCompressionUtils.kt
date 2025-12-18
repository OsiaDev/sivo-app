package com.coljuegos.sivo.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.Deflater

object ImageCompressionUtils {

    private const val BUFFER_SIZE = 4096

    /**
     * Lee archivo de imagen, convierte a PNG sin pérdida, comprime con ZLIB y codifica en Base64
     * Compatible con el método base64ZlibToImage del backend
     */
    fun compressImageFileToBase64Zlib(file: File): String? {
        return try {
            if (!file.exists()) return null

            // Leer archivo como bytes
            val imageBytes = file.readBytes()

            // Decodificar a Bitmap
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                ?: return null

            // Convertir a PNG sin pérdida
            val pngStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, pngStream)
            val pngBytes = pngStream.toByteArray()

            // Comprimir con ZLIB y codificar Base64
            compressBytesToBase64Zlib(pngBytes)

        } catch (_: Exception) {
            null
        }
    }

    /**
     * Convierte Bitmap a PNG sin pérdida, comprime con ZLIB y codifica en Base64
     * Compatible con el método base64ZlibToImage del backend
     * Útil para firmas digitales que ya están en memoria como Bitmap
     */
    fun compressBitmapToBase64Zlib(bitmap: Bitmap): String {
        // Convertir a PNG sin pérdida
        val pngStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, pngStream)
        val pngBytes = pngStream.toByteArray()

        // Comprimir con ZLIB y codificar Base64
        return compressBytesToBase64Zlib(pngBytes)
    }

    /**
     * Comprime bytes con ZLIB y codifica en Base64
     */
    private fun compressBytesToBase64Zlib(bytes: ByteArray): String {
        val deflater = Deflater()
        deflater.setInput(bytes)
        deflater.finish()

        val outputStream = ByteArrayOutputStream()
        val buffer = ByteArray(BUFFER_SIZE)

        try {
            while (!deflater.finished()) {
                val count = deflater.deflate(buffer)
                outputStream.write(buffer, 0, count)
            }

            val compressed = outputStream.toByteArray()
            return Base64.encodeToString(compressed, Base64.NO_WRAP)

        } finally {
            deflater.end()
            outputStream.close()
        }
    }

}