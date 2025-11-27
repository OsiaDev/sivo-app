package com.coljuegos.sivo.workers

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActaSincronizacionWorkManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val workManager = WorkManager.getInstance(context)

    /**
     * Programa sincronización periódica cada 15 minutos
     */
    fun programarSincronizacionPeriodica() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val sincronizacionRequest = PeriodicWorkRequestBuilder<ActaSincronizacionWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            ActaSincronizacionWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            sincronizacionRequest
        )
    }

    /**
     * Ejecuta sincronización inmediata (cuando recupera internet)
     */
    fun ejecutarSincronizacionInmediata() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val sincronizacionRequest = OneTimeWorkRequestBuilder<ActaSincronizacionWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            "${ActaSincronizacionWorker.WORK_NAME}_immediate",
            ExistingWorkPolicy.REPLACE,
            sincronizacionRequest
        )
    }

    /**
     * Cancela todas las sincronizaciones programadas
     */
    fun cancelarSincronizacion() {
        workManager.cancelUniqueWork(ActaSincronizacionWorker.WORK_NAME)
    }

    /**
     * Observa el estado del trabajo de sincronización
     */
    fun observarEstadoSincronizacion() =
        workManager.getWorkInfosForUniqueWorkLiveData(ActaSincronizacionWorker.WORK_NAME)

}