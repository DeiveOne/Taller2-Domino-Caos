package com.udistrital.dominocaos.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.udistrital.dominocaos.domain.Side

@Composable
fun ControlsBar(
    sides: List<Side>, canDraw: Boolean, canPass: Boolean, poolSize: Int,
    onSide: (Side) -> Unit, onDraw: () -> Unit, onPass: () -> Unit
) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (Side.LEFT in sides) Button({ onSide(Side.LEFT) }) { Text("◀ Izquierda") }
        if (Side.RIGHT in sides) Button({ onSide(Side.RIGHT) }) { Text("Derecha ▶") }
        OutlinedButton(onDraw, enabled = canDraw) { Text("Robar ($poolSize)") }
        OutlinedButton(onPass, enabled = canPass) { Text("Pasar") }
    }
}
