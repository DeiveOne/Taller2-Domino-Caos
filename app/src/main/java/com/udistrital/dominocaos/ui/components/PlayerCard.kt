package com.udistrital.dominocaos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udistrital.dominocaos.domain.PlayerState
import kotlinx.coroutines.delay

/** Muestra permanentemente nombre, puntaje y turno de un jugador con soporte de reacciones. */
@Composable
fun PlayerCard(
    p: PlayerState, isTurn: Boolean, isMe: Boolean,
    modifier: Modifier = Modifier, onClick: (() -> Unit)? = null
) {
    // Control local para mostrar/ocultar el emoji temporalmente
    var showEmoji by remember { mutableStateOf(false) }
    var currentEmoji by remember { mutableStateOf("") }

    // Efecto que se dispara cada vez que cambia el 'timestamp' de la reacción en Firebase
    LaunchedEffect(p.reaction?.timestamp) {
        if (p.reaction != null) {
            currentEmoji = p.reaction.emoji
            showEmoji = true
            delay(3000) // El emoji desaparece tras 3 segundos
            showEmoji = false
        }
    }

    // Un Box permite superponer elementos (el emoji encima de la tarjeta)
    Box(contentAlignment = Alignment.TopEnd) {
        Card(
            modifier = modifier
                .padding(top = 10.dp, end = 10.dp) // Damos margen para que el emoji flote por fuera
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
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

        // Emoji animado flotando en la esquina superior derecha
        AnimatedVisibility(
            visible = showEmoji,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Text(
                text = currentEmoji,
                fontSize = 28.sp,
                modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
            )
        }
    }
}