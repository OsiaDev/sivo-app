package com.coljuegos.sivo.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.coljuegos.sivo.data.dao.ActaDao
import com.coljuegos.sivo.data.dao.ImagenDao
import com.coljuegos.sivo.data.remote.api.ApiService
import com.coljuegos.sivo.data.remote.model.ImagenDTO
import com.coljuegos.sivo.data.remote.model.UploadImagenActaDTO
import com.coljuegos.sivo.utils.ImageCompressionUtils
import com.coljuegos.sivo.utils.NetworkConnectivityObserver
import com.coljuegos.sivo.utils.SessionManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File

@HiltWorker
class ImagenSincronizacionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val imagenDao: ImagenDao,
    private val actaDao: ActaDao,
    private val apiService: ApiService,
    private val sessionManager: SessionManager,
    private val networkConnectivityObserver: NetworkConnectivityObserver
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        if (!networkConnectivityObserver.isNetworkAvailable()) {
            return Result.retry()
        }

        val authHeader = sessionManager.getAuthorizationHeader() ?: return Result.failure()

        return try {
            val imagenesPendientes = imagenDao.getImagenesNoSincronizadas()
            if (imagenesPendientes.isEmpty()) {
                return Result.success()
            }

            var allSuccessful = true

            for (imagenEntity in imagenesPendientes) {
                val acta = actaDao.getActaByUuid(imagenEntity.uuidActa) ?: continue
                
                val file = File(imagenEntity.rutaImagen)
                if (!file.exists()) {
                    // Si el archivo no existe, no podemos hacer mucho. 
                    // Podríamos marcarlo como error o ignorarlo. Por ahora ignoramos.
                    continue
                }

                val imagenBase64 = ImageCompressionUtils.compressImageFileToBase64Zlib(file)
                if (imagenBase64 == null) {
                    allSuccessful = false
                    continue
                }

                val imagenDTO = ImagenDTO(
                    nombreImagen = imagenEntity.nombreImagen,
                    imagenBase64 = imagenBase64,
                    descripcion = imagenEntity.descripcion,
                    fragmentOrigen = imagenEntity.fragmentOrigen
                )

                val uploadDTO = UploadImagenActaDTO(
                    numActa = acta.numActa,
                    imagen = imagenDTO
                )

                val response = apiService.uploadImage(authHeader, uploadDTO)
                if (response.isSuccessful && response.body()?.success == true) {
                    imagenDao.marcarComoSincronizada(imagenEntity.uuidImagen)
                } else {
                    allSuccessful = false
                }
            }

            if (allSuccessful) Result.success() else Result.retry()

        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "imagen_sincronizacion_work"
    }
    
}
