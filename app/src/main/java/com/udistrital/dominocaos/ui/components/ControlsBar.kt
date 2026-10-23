package com.udistrital.dominocaos.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.udistrital.dominocaos.domain.Side

@Composable
fun ControlsBar(
    sides: List<Side>, canDraw: Boolean, canPass: Boolean, poolSize: Int,
    onSide: (Side) -> Unit, onDraw: () -> Unit, onPass: () -> Unit
) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (Side.LEFT in sides) {
            Button(
                onClick = { onSide(Side.LEFT) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE040FB)),
                shape = RoundedCornerShape(12.dp)
            ) { Text("◀ Izquierda", fontWeight = FontWeight.Bold, color = Color.White) }
        }
        if (Side.RIGHT in sides) {
            Button(
                onClick = { onSide(Side.RIGHT) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE040FB)),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Derecha ▶", fontWeight = FontWeight.Bold, color = Color.White) }
        }

        OutlinedButton(
            onClick = onDraw, enabled = canDraw,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White, // Asegura que el efecto de toque (ripple) sea blanco
                disabledContentColor = Color.White.copy(alpha = 0.5f) // Blanco atenuado si no es su turno
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF00E5FF)) // Mantiene el borde Cyan
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            // Color de fuente blanco forzado
            Text("Robar ($poolSize)", fontWeight = FontWeight.Bold, color = Color.White)
        }

        OutlinedButton(
            onClick = onPass, enabled = canPass,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White,
                disabledContentColor = Color.White.copy(alpha = 0.5f)
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFF5252)) // Mantiene el borde Rojo
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            // Color de fuente blanco forzado
            Text("Pasar", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}