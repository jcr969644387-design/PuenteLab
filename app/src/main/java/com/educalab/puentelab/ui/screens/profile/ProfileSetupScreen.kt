package com.educalab.puentelab.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.educalab.puentelab.data.seed.AvatarCatalog
import com.educalab.puentelab.ui.components.AvatarPortrait
import com.educalab.puentelab.ui.theme.*

@Composable
fun ProfileSetupScreen(onConfirm: (alias: String, avatarId: String) -> Unit) {
    var alias by remember { mutableStateOf("") }
    var selectedAvatar by remember { mutableStateOf(AvatarCatalog.all.first().id) }
    val validName = alias.trim().isNotEmpty()

    Surface(color = PaperBg, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(24.dp)) {
            Text("Elige tu apodo de ingeniero/a", style = MaterialTheme.typography.titleLarge, color = Blueprint900)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = alias,
                onValueChange = { if (it.length <= 18) alias = it },
                placeholder = { Text("Ej: ArcoVeloz, LunaAcero…") },
                singleLine = true,
                isError = !validName,
                supportingText = {
                    if (!validName) {
                        Text("Escribe tu nombre para comenzar tu aventura.", color = WarningRed)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(28.dp))
            Text("Elige tu avatar", style = MaterialTheme.typography.titleLarge, color = Blueprint900)
            Spacer(Modifier.height(12.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(AvatarCatalog.all) { avatar ->
                    val selected = avatar.id == selectedAvatar
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .border(width = if (selected) 3.dp else 0.dp, color = SiteOrange, shape = CircleShape)
                            .clickable { selectedAvatar = avatar.id },
                        contentAlignment = Alignment.Center
                    ) {
                        AvatarPortrait(avatar, modifier = Modifier.fillMaxSize(0.9f))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onConfirm(alias.trim(), selectedAvatar) },
                enabled = validName,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SiteOrange)
            ) {
                Text("Empezar a construir")
            }
        }
    }
}
