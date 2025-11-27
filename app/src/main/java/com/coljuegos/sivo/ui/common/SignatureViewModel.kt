package com.coljuegos.sivo.ui.common

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SignatureViewModel @Inject constructor() : ViewModel() {

    private val _signatureBitmap = MutableStateFlow<Bitmap?>(null)
    val signatureBitmap: StateFlow<Bitmap?> = _signatureBitmap.asStateFlow()

    private val _signatureType = MutableStateFlow<Int>(0) // 0 = PRINCIPAL por defecto
    val signatureType: StateFlow<Int> = _signatureType.asStateFlow()

    fun setSignatureType(type: Int) {
        _signatureType.value = type
    }

    fun saveSignature(bitmap: Bitmap) {
        _signatureBitmap.value = bitmap
    }

    fun clearSignature() {
        _signatureBitmap.value = null
    }

    fun hasSignature(): Boolean = _signatureBitmap.value != null

}