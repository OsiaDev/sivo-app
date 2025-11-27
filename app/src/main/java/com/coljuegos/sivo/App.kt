package com.coljuegos.sivo

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.coljuegos.sivo.utils.DataLoaderService
import com.coljuegos.sivo.workers.ActaSincronizacionWorkManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var actaSincronizacionWorkManager: ActaSincronizacionWorkManager

    @Inject
    lateinit var dataLoaderService: DataLoaderService

    override fun onCreate() {
        super.onCreate()

        // Cargar datos iniciales en background
        CoroutineScope(Dispatchers.IO).launch {
            dataLoaderService.loadLocationData()
        }

        actaSincronizacionWorkManager.programarSincronizacionPeriodica()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

}