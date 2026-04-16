package com.coljuegos.sivo.workers

import android.content.Context
import android.util.Log
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
import kotlinx.coroutines.CancellationException
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

    companion object {
        const val WORK_NAME = "imagen_sincronizacion_work"
        private const val TAG = "ImagenSincWorker"

        /**
         * Número de confirmaciones exitosas del backend requeridas para considerar
         * una imagen como definitivamente sincronizada.
         * - 1ra confirmación: el backend aceptó la imagen.
         * - 2da confirmación: re-envío posterior también exitoso (backend idempotente),
         *   garantizando que realmente persiste en el servidor.
         */
        private const val VERIFICACIONES_REQUERIDAS = 2
    }

    override suspend fun doWork(): Result {
        if (!networkConnectivityObserver.isNetworkAvailable()) {
            Log.d(TAG, "Sin red disponible. Se reintentará más tarde.")
            return Result.retry()
        }

        val authHeader = sessionManager.getAuthorizationHeader() ?: run {
            Log.e(TAG, "Sin sesión activa. No se puede sincronizar.")
            return Result.failure()
        }

        return try {
            // Solo trae imágenes con isSincronizada=0 y verificacionesConfirmadas < 2.
            // Las que ya tienen isSincronizada=1 nunca aparecen aquí.
            val imagenesPendientes = imagenDao.getImagenesPendientesDeVerificacion()

            if (imagenesPendientes.isEmpty()) {
                Log.d(TAG, "No hay imágenes pendientes de verificación.")
                return Result.success()
            }

            Log.i(TAG, "Procesando ${imagenesPendientes.size} imagen(es) pendiente(s).")

            var todasVerificadas = true

            for (imagenEntity in imagenesPendientes) {

                // Re-leer desde Room antes de procesar cada imagen para tener
                // el estado real persistido, no el estado en memoria de la lista
                val imagenActual = imagenDao.getImagenById(imagenEntity.uuidImagen)
                if (imagenActual == null) {
                    Log.w(TAG, "Imagen ${imagenEntity.nombreImagen} ya no existe en BD. Saltando.")
                    continue
                }
                if (imagenActual.isSincronizada) {
                    Log.d(TAG, "Imagen ${imagenEntity.nombreImagen} ya está sincronizada en BD. Saltando.")
                    continue
                }

                try {
                    val acta = actaDao.getActaByUuid(imagenEntity.uuidActa)
                    if (acta == null) {
                        val msg = "Acta no encontrada para imagen ${imagenEntity.nombreImagen}"
                        Log.e(TAG, msg)
                        imagenDao.setError(imagenEntity.uuidImagen, msg)
                        todasVerificadas = false
                        continue
                    }

                    val file = File(imagenEntity.rutaImagen)
                    if (!file.exists()) {
                        val msg = "Archivo físico no encontrado: ${imagenEntity.rutaImagen}"
                        Log.e(TAG, msg)
                        imagenDao.setError(imagenEntity.uuidImagen, msg)
                        todasVerificadas = false
                        continue
                    }

                    val imagenBase64 = ImageCompressionUtils.compressImageFileToBase64Zlib(file)
                    if (imagenBase64 == null) {
                        val msg = "Error al comprimir imagen ${imagenEntity.nombreImagen}"
                        Log.e(TAG, msg)
                        imagenDao.setError(imagenEntity.uuidImagen, msg)
                        todasVerificadas = false
                        continue
                    }

                    val uploadDTO = UploadImagenActaDTO(
                        numActa = acta.numActa,
                        imagen = ImagenDTO(
                            nombreImagen = imagenEntity.nombreImagen,
                            imagenBase64 = imagenBase64,
                            descripcion = imagenEntity.descripcion,
                            fragmentOrigen = imagenEntity.fragmentOrigen
                        )
                    )

                    val response = apiService.uploadImage(authHeader, uploadDTO)

                    if (response.isSuccessful && response.body()?.success == true) {
                        // Backend confirmó que la imagen existe (nueva o ya existente por idempotencia)
                        imagenDao.incrementarVerificaciones(imagenEntity.uuidImagen)
                        imagenDao.setError(imagenEntity.uuidImagen, null)

                        val verificacionesAhora = imagenActual.verificacionesConfirmadas + 1
                        Log.i(TAG, "✓ Confirmación $verificacionesAhora/$VERIFICACIONES_REQUERIDAS para ${imagenEntity.nombreImagen}")

                        if (verificacionesAhora >= VERIFICACIONES_REQUERIDAS) {
                            // Marcar como sincronizada definitivamente en Room
                            imagenDao.marcarSincronizadaDefinitivamente(imagenEntity.uuidImagen)
                            Log.i(TAG, "✓✓ ${imagenEntity.nombreImagen} verificada $VERIFICACIONES_REQUERIDAS veces. SINCRONIZADA definitivamente.")
                        } else {
                            // Necesita una verificación más en la próxima ejecución
                            Log.i(TAG, "→ ${imagenEntity.nombreImagen} necesita ${VERIFICACIONES_REQUERIDAS - verificacionesAhora} verificación(es) más.")
                            todasVerificadas = false
                        }

                    } else {
                        val errorMsg = response.errorBody()?.string()
                            ?: response.message()
                            ?: "Error desconocido del servidor"
                        imagenDao.setError(imagenEntity.uuidImagen, errorMsg)
                        Log.w(TAG, "✗ Backend rechazó ${imagenEntity.nombreImagen}: $errorMsg")
                        todasVerificadas = false
                    }

                } catch (e: CancellationException) {
                    throw e  // Siempre relanzar para que WorkManager gestione la cancelación
                } catch (e: Exception) {
                    val errorMsg = e.message ?: "Error inesperado"
                    Log.e(TAG, "Excepción procesando ${imagenEntity.nombreImagen}: $errorMsg", e)
                    imagenDao.setError(imagenEntity.uuidImagen, errorMsg)
                    todasVerificadas = false
                    // verificacionesConfirmadas no se incrementa: la imagen
                    // vuelve a intentarse en la próxima ejecución del worker
                }
            }

            // success solo si todas llegaron a sus 2 verificaciones
            // retry si alguna aún necesita más confirmaciones o falló
            if (todasVerificadas) Result.success() else Result.retry()

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error global en ImagenSincronizacionWorker: ${e.message}", e)
            Result.retry()
        }
    }

}