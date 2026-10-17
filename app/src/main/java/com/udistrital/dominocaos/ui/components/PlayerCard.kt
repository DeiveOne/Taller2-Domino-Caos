package com.udistrital.dominocaos.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.udistrital.dominocaos.domain.PlayerState

/** Muestra permanentemente nombre, puntaje y turno de un jugador. */
@Composable
fun PlayerCard(
    p: PlayerState, isTurn: Boolean, isMe: Boolean,
    modifier: Modifier = Modifier, onClick: (() -> Unit)? = null
) {
    Card(
        modifier.then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(
            containerColor = if (isTurn) Color(0xFFFFE082) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(10.dp)) {
            Text(p.name + if (isMe) " (tú)" else "", fontWeight = FontWeight.Bold)
            Text("Puntaje: ${p.score}")
            Text("Fichas: ${p.hand.size} · Poderes usados: ${p.powersUsed}", style = MaterialTheme.typography.bodySmall)
            if (isTurn) Text("▶ Turno actual", color = Color(0xFFB71C1C))
        }
    }
}
