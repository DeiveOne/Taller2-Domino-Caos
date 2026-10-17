package com.udistrital.dominocaos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.udistrital.dominocaos.viewmodel.LobbyViewModel

@Composable
fun LobbyScreen(onRoom: (String) -> Unit, onSignOut: () -> Unit, vm: LobbyViewModel = viewModel()) {
    var code by remember { mutableStateOf("") }
    var rounds by remember { mutableFloatStateOf(10f) }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Salas", style = MaterialTheme.typography.headlineMedium)
        Text("Rondas máximas: ${rounds.toInt()}")
        Slider(rounds, { rounds = it }, valueRange = 4f..20f, enabled = !vm.loading)
        
        Button(
            onClick = { vm.create(rounds.toInt(), onRoom) },
            enabled = !vm.loading
        ) {
            if (vm.loading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text("Crear sala")
        }
        
        HorizontalDivider()
        
        OutlinedTextField(
            value = code,
            onValueChange = { code = it.uppercase().take(5) },
            label = { Text("Código de sala") },
            singleLine = true,
            enabled = !vm.loading
        )
        
        Button(
            onClick = { vm.join(code, onRoom) },
            enabled = code.length == 5 && !vm.loading
        ) {
            Text("Unirse")
        }
        
        vm.error?.let {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(12.dp).fillMaxWidth()
                )
            }
        }
        
        TextButton(onClick = onSignOut, enabled = !vm.loading) {
            Text("Cerrar sesión")
        }
    }
}
