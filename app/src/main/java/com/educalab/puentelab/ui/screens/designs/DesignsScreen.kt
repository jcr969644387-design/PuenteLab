package com.educalab.puentelab.ui.screens.designs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.educalab.puentelab.domain.model.BridgeDesignSpec
import com.educalab.puentelab.ui.theme.*
import com.educalab.puentelab.ui.viewmodel.DesignsViewModel

@Composable
fun DesignsScreen(viewModel: DesignsViewModel) {
    val designs by viewModel.savedDesigns.collectAsStateWithLifecycle()

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
                    items(designs, key = { it.id }) { design ->
                        DesignRow(
                            design = design,
                            onDuplicate = { viewModel.duplicate(design.id, "${design.name} (copia)") { } },
                            onDelete = { viewModel.delete(design.id) }
                        )
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun DesignRow(design: BridgeDesignSpec, onDuplicate: () -> Unit, onDelete: () -> Unit) {
    Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(design.name, style = MaterialTheme.typography.titleMedium)
                Text("${design.nodes.size} nodos · ${design.members.size} barras", style = MaterialTheme.typography.bodyMedium, color = Ink600)
            }
            IconButton(onClick = onDuplicate) { Icon(Icons.Filled.ContentCopy, contentDescription = "Duplicar") }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = WarningRed) }
        }
    }
}
