package com.educalab.puentelab

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

/**
 * Pantalla de emergencia: se muestra en vez del cierre silencioso de la app cuando ocurre
 * un error no controlado. Corre en un proceso Android separado (ver AndroidManifest) para
 * no depender del proceso que acaba de fallar.
 */
class CrashReportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val trace = intent.getStringExtra(EXTRA_TRACE) ?: "No se pudo capturar el detalle del error."

        setContent {
            MaterialTheme {
                Surface(color = Color(0xFF1C2530)) {
                    Column(Modifier.fillMaxSize().padding(20.dp)) {
                        Text("PuenteLab encontró un error", color = Color.White, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Copia este texto y envíalo para poder corregirlo:",
                            color = Color(0xFFDCEBFA), style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { copyToClipboard(trace) }) {
                            Text("Copiar error")
                        }
                        Spacer(Modifier.height(16.dp))
                        SelectionContainer(Modifier.verticalScroll(rememberScrollState())) {
                            Text(trace, color = Color(0xFFF5C34C), fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }

    private fun copyToClipboard(trace: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("PuenteLab crash", trace))
        Toast.makeText(this, "Error copiado", Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_TRACE = "extra_trace"
    }
}
