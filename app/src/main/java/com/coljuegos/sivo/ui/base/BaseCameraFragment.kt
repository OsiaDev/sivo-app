package com.coljuegos.sivo.ui.base

import android.net.Uri
import androidx.fragment.app.Fragment

abstract class BaseCameraFragment : Fragment() {

    abstract fun handleCapturedImage(imageUri: Uri)

}