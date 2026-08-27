package com.educalab.puentelab.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.educalab.puentelab.ui.theme.*
import com.educalab.puentelab.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: ProfileViewModel, onBack: () -> Unit) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = PaperBg,
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Volver") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Blueprint700, titleContentColor = White, navigationIconContentColor = White)
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
            Text("Sonido y vibración", style = MaterialTheme.typography.titleLarge, color = Blueprint900)
            Spacer(Modifier.height(4.dp))
            Text(
                "Actívalos o desactívalos cuando quieras. No afectan tu progreso.",
                style = MaterialTheme.typography.bodyMedium, color = Ink600
            )
            Spacer(Modifier.height(16.dp))

            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column {
                    SettingRow(
                        icon = Icons.Filled.VolumeUp,
                        title = "Sonido",
                        subtitle = "Clics, aciertos y fallos con sonido.",
                        checked = profile?.soundEnabled ?: true,
                        onCheckedChange = { viewModel.setSoundEnabled(it) }
                    )
                    Divider()
                    SettingRow(
                        icon = Icons.Filled.Vibration,
                        title = "Vibración",
                        subtitle = "El teléfono vibra al tocar piezas y resultados.",
                        checked = profile?.hapticEnabled ?: true,
                        onCheckedChange = { viewModel.setHapticEnabled(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Blueprint700)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Ink900)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Ink600)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = SiteOrange)
        )
    }
}
