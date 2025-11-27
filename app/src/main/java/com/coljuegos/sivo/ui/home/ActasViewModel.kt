package com.coljuegos.sivo.ui.home

import androidx.lifecycle.ViewModel
import com.coljuegos.sivo.data.repository.ActasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ActasViewModel @Inject constructor(
    private val actasRepository: ActasRepository
) : ViewModel() {

}