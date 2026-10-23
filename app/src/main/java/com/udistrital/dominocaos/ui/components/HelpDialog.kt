package com.udistrital.dominocaos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.udistrital.dominocaos.domain.EventType
import com.udistrital.dominocaos.domain.Power

@Composable
fun HelpDialog(onDismiss: () -> Unit) {
    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("📖 Reglas", "⚡ Poderes", "🎲 Eventos")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Guía de Dominó Caos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(selectedTabIndex = tabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = tabIndex == index,
                            onClick = { tabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.fillMaxHeight(0.6f)) {
                    when (tabIndex) {
                        0 -> ComoJugarTab()
                        1 -> PoderesTab()
                        2 -> EventosTab()
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("¡A jugar!") }
        }
    )
}

@Composable
private fun ComoJugarTab() {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("🎯 Objetivo", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Vacía tu mano de fichas o sé el jugador con más puntos al finalizar el límite de rondas.", style = MaterialTheme.typography.bodyMedium)
        }
        item {
            Text("🔄 Tu Turno", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("1. Si vas a usar un Poder, selecciónalo primero en la barra inferior.\n2. Toca una ficha de tu mano.\n3. Elige 'Izquierda' o 'Derecha' para colocarla.", style = MaterialTheme.typography.bodyMedium)
        }
        item {
            Text("📥 Robar y Pasar", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Si no tienes ninguna ficha que encaje en el tablero, debes tocar 'Robar'. Si el pozo se queda sin fichas y sigues sin poder jugar, presiona 'Pasar'.", style = MaterialTheme.typography.bodyMedium)
        }
        item {
            Text("⭐ Puntuación", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Ganas los puntos que sumen los números de tu ficha. Las fichas dobles (ej: 6-6) dan un bono de +2 pts. Si eres el primero en vaciar tu mano, recibes un bono final de +10 pts.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun PoderesTab() {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Los poderes te dan ventajas tácticas. Selecciona uno ANTES de elegir tu ficha.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
        }
        items(Power.entries) { p ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(p.label, fontWeight = FontWeight.Bold)
                        Badge { Text("${p.initialUses} usos") }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = when (p) {
                            Power.WILDCARD -> "Te permite colocar tu ficha aunque los números no coincidan con el extremo del tablero."
                            Power.SKIP -> "Colocas tu ficha e inmediatamente el siguiente jugador pierde su turno."
                            Power.SWAP -> "Eliges tu ficha, tocas la tarjeta de un oponente y robas una ficha al azar de su mano a cambio de la tuya."
                            Power.REVERSE -> "Colocas tu ficha y el orden de los turnos cambia de dirección."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun EventosTab() {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Cada cierta cantidad de turnos, el caos interviene y se dispara un evento global al azar que afecta a todos.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
        }
        items(EventType.entries) { e ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⚠️", modifier = Modifier.padding(end = 8.dp))
                Text(e.label, style = MaterialTheme.typography.bodyMedium)
            }
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
        }
    }
}