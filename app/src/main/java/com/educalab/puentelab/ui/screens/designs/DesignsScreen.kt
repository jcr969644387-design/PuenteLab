package com.educalab.puentelab.ui.screens.designs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.educalab.puentelab.ui.components.DesignThumbnail
import com.educalab.puentelab.ui.theme.*
import com.educalab.puentelab.ui.viewmodel.DesignsViewModel
import com.educalab.puentelab.ui.viewmodel.SavedDesignUiItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DesignsScreen(viewModel: DesignsViewModel, onOpenDesign: (String) -> Unit) {
    val designs by viewModel.savedDesigns.collectAsStateWithLifecycle()
    var renamingDesign by remember { mutableStateOf<SavedDesignUiItem?>(null) }

    Surface(color = PaperBg, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text("Mis Diseños", style = MaterialTheme.typography.headlineMedium, color = Blueprint900)
            Text("${designs.size} / ${viewModel.maxDesigns} guardados", style = MaterialTheme.typography.bodyMedium, color = Ink600)
            Spacer(Modifier.height(12.dp))
            if (designs.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Aún no guardaste ningún puente.\nCompleta un desafío y toca “Guardar diseño”.", textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Ink600)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(designs, key = { it.design.id }) { item ->
                        DesignRow(
                            item = item,
                            onOpen = { onOpenDesign(item.design.id) },
                            onRename = { renamingDesign = item },
                            onDuplicate = { viewModel.duplicate(item.design.id, "${item.design.name} (copia)") { } },
                            onDelete = { viewModel.delete(item.design.id) }
                        )
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }

    val toRename = renamingDesign
    if (toRename != null) {
        RenameDesignDialog(
            initialName = toRename.design.name,
            onConfirm = { newName ->
                viewModel.rename(toRename.design.id, newName) { }
                renamingDesign = null
            },
            onDismiss = { renamingDesign = null }
        )
    }
}

@Composable
private fun DesignRow(item: SavedDesignUiItem, onOpen: () -> Unit, onRename: () -> Unit, onDuplicate: () -> Unit, onDelete: () -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }
    Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)).background(Blueprint100),
                contentAlignment = Alignment.Center
            ) {
                DesignThumbnail(item.design, modifier = Modifier.fillMaxSize().padding(6.dp))
            }
            Spacer(Modifier.width(12.dp))
            // weight(1f) le da a esta columna todo el ancho sobrante: así el nombre tiene
            // espacio real para truncarse con "..." en vez de partirse letra por letra.
            Column(Modifier.weight(1f)) {
                Text(
                    item.design.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    item.challenge?.let { "${it.scenario.displayName} · Misión ${it.orderIndex}" } ?: "Desafío",
                    style = MaterialTheme.typography.bodySmall, color = Ink600,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(formatSavedDate(item.updatedAt), style = MaterialTheme.typography.bodySmall, color = Ink600)
            }
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onOpen) { Icon(Icons.Filled.PlayArrow, contentDescription = "Abrir", tint = SuccessGreen) }
            Box {
                IconButton(onClick = { menuExpanded = true }) { Icon(Icons.Filled.MoreVert, contentDescription = "Más opciones") }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Renombrar") },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                        onClick = { menuExpanded = false; onRename() }
                    )
                    DropdownMenuItem(
                        text = { Text("Duplicar") },
                        leadingIcon = { Icon(Icons.Filled.ContentCopy, contentDescription = null) },
                        onClick = { menuExpanded = false; onDuplicate() }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar") },
                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = WarningRed) },
                        onClick = { menuExpanded = false; onDelete() }
                    )
                }
            }
        }
    }
}

@Composable
private fun RenameDesignDialog(initialName: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Renombrar diseño") },
        text = {
            OutlinedTextField(value = name, onValueChange = { if (it.length <= 24) name = it }, singleLine = true)
        },
        confirmButton = { TextButton(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

private fun formatSavedDate(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(millis))
