package com.coljuegos.sivo.di

object Extenxion {

    fun String.orNA() = ifEmpty { "N/A" }

    fun Long.toReadableFileSize(): String {
        val bytes = this
        return when {
            bytes < 1024 -> "${bytes}B"
            bytes < 1024 * 1024 -> "${(bytes / 1024.0).format(1)} KB"
            bytes < 1024 * 1024 * 1024 -> "${(bytes / (1024.0 * 1024.0)).format(1)} MB"
            bytes < 1024L * 1024 * 1024 * 1024 -> "${(bytes / (1024.0 * 1024.0 * 1024.0)).format(1)} GB"
            else -> "${(bytes / (1024.0 * 1024.0 * 1024.0 * 1024.0)).format(1)} TB"
        }
    }

    private fun Double.format(decimals: Int): String {
        return if (this % 1.0 == 0.0) {
            "%.0f".format(this)
        } else {
            "%.${decimals}f".format(this)
        }
    }

}