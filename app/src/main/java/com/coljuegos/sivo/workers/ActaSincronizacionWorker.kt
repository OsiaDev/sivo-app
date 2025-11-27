package com.coljuegos.sivo.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.coljuegos.sivo.data.repository.ActaSincronizacionRepository
import com.coljuegos.sivo.utils.NetworkConnectivityObserver
import com.coljuegos.sivo.utils.NetworkResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ActaSincronizacionWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val actaSincronizacionRepository: ActaSincronizacionRepository,
    private val networkConnectivityObserver: NetworkConnectivityObserver
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // Verificar si hay conexión a internet
        if (!networkConnectivityObserver.isNetworkAvailable()) {
            return Result.retry()
        }

        return try {
            // Obtener actas pendientes de sincronización
            val actasPendientes = actaSincronizacionRepository.getActasPendientesSincronizacion()

            if (actasPendientes.isEmpty()) {
                return Result.success()
            }

            var todosSincronizados = true
            var algunoSincronizado = false

            // Intentar sincronizar cada acta
            actasPendientes.forEach { acta ->
                actaSincronizacionRepository.sincronizarActaConBackend(acta.uuidActa)
                    .collect { result ->
                        when (result) {
                            is NetworkResult.Success -> {
                                algunoSincronizado = true
                            }
                            is NetworkResult.Error -> {
                                todosSincronizados = false
                            }
                            is NetworkResult.Loading -> {
                                // Ignorar estado de carga
                            }
                        }
                    }
            }

            when {
                todosSincronizados -> Result.success()
                algunoSincronizado -> Result.retry() // Algunos fallaron, reintentar después
                else -> Result.retry() // Todos fallaron, reintentar después
            }

        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "acta_sincronizacion_work"
    }

}