package com.udistrital.dominocaos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun PlayerCard(
    p: PlayerState, isTurn: Boolean, isMe: Boolean,
    modifier: Modifier = Modifier, onClick: (() -> Unit)? = null
) {
    var showEmoji by remember { mutableStateOf(false) }
    var currentEmoji by remember { mutableStateOf("") }

    LaunchedEffect(p.reaction?.timestamp) {
        if (p.reaction != null) {
            currentEmoji = p.reaction.emoji
            showEmoji = true
            delay(3000)
            showEmoji = false
        }
    }

    // Colores Neón para el turno activo
    val borderColor = if (isTurn) Color(0xFF00E5FF) else Color.Transparent
    val bgColor = if (isTurn) Color(0xFF00E5FF).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)

    Box(contentAlignment = Alignment.TopEnd) {
        Card(
            modifier = modifier
                .padding(top = 10.dp, end = 10.dp)
                .border(if (isTurn) 2.dp else 0.dp, borderColor, RoundedCornerShape(12.dp))
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
            colors = CardDefaults.cardColors(containerColor = bgColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(
                    text = p.name + if (isMe) " (Tú)" else "",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Puntaje: ${p.score}",
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = "Fichas: ${p.hand.size} · Poderes: ${p.powersUsed}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
                if (isTurn) {
                    Text("▶ TU TURNO", color = Color(0xFF00E5FF), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        AnimatedVisibility(
            visible = showEmoji, enter = fadeIn(), exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Text(currentEmoji, fontSize = 28.sp, modifier = Modifier.padding(bottom = 4.dp, start = 4.dp))
        }
    }
}