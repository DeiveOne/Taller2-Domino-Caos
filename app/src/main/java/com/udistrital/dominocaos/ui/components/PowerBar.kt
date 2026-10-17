package com.udistrital.dominocaos.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.udistrital.dominocaos.domain.PlayerState
import com.udistrital.dominocaos.domain.Power

/** Selector de poderes con usos restantes. Se elige ANTES de mover. */
@Composable
fun PowerBar(me: PlayerState, selected: Power?, enabled: Boolean, onSelect: (Power) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Power.entries.forEach { p ->
            val left = me.powers[p] ?: 0
            FilterChip(
                selected = selected == p, onClick = { onSelect(p) },
                enabled = enabled && left > 0, label = { Text("${p.label} ($left)") }
            )
        }
    }
}
