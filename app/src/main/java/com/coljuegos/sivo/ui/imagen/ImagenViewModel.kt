package com.coljuegos.sivo.ui.imagen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coljuegos.sivo.data.dao.ImagenDao
import com.coljuegos.sivo.data.entity.ImagenEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ImagenViewModel @Inject constructor(
    private val imagenDao: ImagenDao
) : ViewModel() {

    private val _imagenes = MutableStateFlow<List<ImagenEntity>>(emptyList())
    val imagenes: StateFlow<List<ImagenEntity>> = _imagenes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadImagenesByActa(uuidActa: UUID) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val imagenes = imagenDao.getImagenesByActa(uuidActa)
                _imagenes.value = imagenes
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar imágenes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadImagenesByActaAndFragment(uuidActa: UUID, fragmentOrigen: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val imagenes = imagenDao.getImagenesByActaAndFragment(uuidActa, fragmentOrigen)
                _imagenes.value = imagenes
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar imágenes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveImagen(uuidActa: UUID, fragmentOrigen: String = "general", rutaImagen: String, nombreImagen: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Obtener tamaño del archivo
                val file = File(rutaImagen)
                val tamanioBytes = if (file.exists()) file.length() else 0L

                val nuevaImagen = ImagenEntity(
                    uuidActa = uuidActa,
                    rutaImagen = rutaImagen,
                    nombreImagen = nombreImagen,
                    tamanoBytesImagen = tamanioBytes,
                    fragmentOrigen = fragmentOrigen
                )

                imagenDao.insertImagen(nuevaImagen)

                // Recargar las imágenes
                loadImagenesByActaAndFragment(uuidActa, fragmentOrigen)
            } catch (e: Exception) {
                _errorMessage.value = "Error al guardar imagen: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteImagen(imagen: ImagenEntity) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Eliminar archivo físico
                val file = File(imagen.rutaImagen)
                if (file.exists()) {
                    file.delete()
                }

                // Eliminar de la base de datos
                imagenDao.deleteImagen(imagen)

                // Recargar las imágenes
                imagen.fragmentOrigen?.let {
                    loadImagenesByActaAndFragment(imagen.uuidActa, it)
                } ?: loadImagenesByActa(imagen.uuidActa)
            } catch (e: Exception) {
                _errorMessage.value = "Error al eliminar imagen: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

}