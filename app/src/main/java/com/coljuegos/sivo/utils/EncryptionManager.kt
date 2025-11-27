package com.coljuegos.sivo.utils

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptionManager @Inject constructor() {

    companion object {
        private const val ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/ECB/PKCS5Padding"
        private const val SECRET_KEY = "QwErTyUiOpAsDfGh"
    }

    @Throws(Exception::class)
    fun encrypt(plainText: String): String {
        return encrypt(plainText, SECRET_KEY)
    }

    @Throws(Exception::class)
    fun encrypt(plainText: String, secretKey: String): String {
        try {
            if (plainText.isBlank() || secretKey.isBlank()) {
                throw IllegalArgumentException("El texto y la clave no pueden estar vacíos")
            }
            val keyBytes = secretKey.toByteArray()
            val keySpec = SecretKeySpec(keyBytes, ALGORITHM)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)

            val encrypted = cipher.doFinal(plainText.toByteArray())
            return Base64.encodeToString(encrypted, Base64.NO_WRAP)

        } catch (e: Exception) {
            throw Exception("Error al encriptar la contraseña: ${e.message}", e)
        }
    }

    @Throws(Exception::class)
    fun decrypt(encryptedText: String): String {
        return decrypt(encryptedText, SECRET_KEY)
    }

    @Throws(Exception::class)
    fun decrypt(encryptedText: String, secretKey: String): String {
        try {
            val keyBytes = secretKey.toByteArray()
            val keySpec = SecretKeySpec(keyBytes, ALGORITHM)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, keySpec)

            val decodedBytes = Base64.decode(encryptedText, Base64.DEFAULT)
            val decrypted = cipher.doFinal(decodedBytes)

            return String(decrypted)

        } catch (e: Exception) {
            throw Exception("Error al desencriptar: ${e.message}", e)
        }
    }

}