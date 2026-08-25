package com.educalab.puentelab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
            PuenteLabTheme {
                PuenteLabNavGraph(viewModelFactory = factory)
            }
        }
    }
}
