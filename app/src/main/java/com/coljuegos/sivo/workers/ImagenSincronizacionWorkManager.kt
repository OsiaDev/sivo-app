package com.coljuegos.sivo.workers

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImagenSincronizacionWorkManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    fun ejecutarSincronizacionInmediata() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<ImagenSincronizacionWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniqueWork(
            "${ImagenSincronizacionWorker.WORK_NAME}_immediate",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun programarSincronizacionPeriodica() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<ImagenSincronizacionWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            ImagenSincronizacionWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
    
}
