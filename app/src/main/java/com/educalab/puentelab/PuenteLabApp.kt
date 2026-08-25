package com.educalab.puentelab

import android.app.Application
import com.educalab.puentelab.util.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PuenteLabApp : Application() {
    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        appScope.launch {
            container.seeder.seedIfNeeded()
            container.profileRepository.getOrCreateProfile()
        }
    }
}
