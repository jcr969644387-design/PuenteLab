package com.educalab.puentelab

import android.app.Application
import android.content.Intent
import android.os.Process
import android.util.Log
import com.educalab.puentelab.util.AppContainer
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class PuenteLabApp : Application() {
    lateinit var container: AppContainer
        private set

    private val startupExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("PuenteLabApp", "Fallo al inicializar datos locales", throwable)
    }
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + startupExceptionHandler)

    override fun onCreate() {
        super.onCreate()
        installCrashReporter()
        container = AppContainer(this)
        appScope.launch {
            container.seeder.seedIfNeeded()
            container.profileRepository.getOrCreateProfile()
        }
    }

    /**
     * Reemplaza el cierre silencioso por defecto: muestra el stack trace en una pantalla
     * copiable (CrashReportActivity, en su propio proceso) en vez de dejar que Android
     * simplemente mate la app sin dar ninguna pista de la causa.
     */
    private fun installCrashReporter() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val trace = Log.getStackTraceString(throwable)
                Log.e("PuenteLabApp", "Uncaught exception", throwable)
                startActivity(
                    Intent(this, CrashReportActivity::class.java).apply {
                        putExtra(CrashReportActivity.EXTRA_TRACE, trace)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                )
                Process.killProcess(Process.myPid())
                exitProcess(10)
            } catch (secondary: Throwable) {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }
}
