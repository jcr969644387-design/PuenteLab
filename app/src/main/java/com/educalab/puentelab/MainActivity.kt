package com.educalab.puentelab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.educalab.puentelab.ui.navigation.PuenteLabNavGraph
import com.educalab.puentelab.ui.theme.PuenteLabTheme
import com.educalab.puentelab.ui.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as PuenteLabApp
        val factory = ViewModelFactory(app.container)

        setContent {
            // La versión de Compose UI del proyecto (BOM 2024.06.00) todavía no provee
            // automáticamente androidx.lifecycle.compose.LocalLifecycleOwner (eso se agregó
            // en Compose UI 1.7.0), lo que hace fallar collectAsStateWithLifecycle() en toda
            // la app con "CompositionLocal LocalLifecycleOwner not present". Se provee a mano
            // usando la propia Activity, que ya es un LifecycleOwner.
            CompositionLocalProvider(LocalLifecycleOwner provides this) {
                PuenteLabTheme {
                    // enableEdgeToEdge() dibuja el contenido detrás de la barra de estado, el
                    // notch/isla dinámica y la barra de navegación. Este padding único, aplicado
                    // una sola vez en la raíz, evita que el contenido de CUALQUIER pantalla quede
                    // tapado por esas zonas, sin tener que repetirlo pantalla por pantalla.
                    Surface(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
                        PuenteLabNavGraph(viewModelFactory = factory)
                    }
                }
            }
        }
    }
}
