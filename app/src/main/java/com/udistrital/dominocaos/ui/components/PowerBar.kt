package com.udistrital.dominocaos.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.udistrital.dominocaos.domain.PlayerState
import com.udistrital.dominocaos.domain.Power

@Composable
fun PowerBar(me: PlayerState, selected: Power?, enabled: Boolean, onSelect: (Power) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Power.entries.forEach { p ->
            val left = me.powers[p] ?: 0
            val isSelected = selected == p

            // Asignación de Emojis a los poderes
            val emoji = when(p) {
                Power.WILDCARD -> "🃏"
                Power.SKIP -> "⏭️"
                Power.SWAP -> "🔀"
                Power.REVERSE -> "🔄"
            }

            FilterChip(
                selected = isSelected,
                onClick = { onSelect(p) },
                enabled = enabled && left > 0,
                label = {
                    Text(
                        text = "$emoji ${p.label} ($left)",
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White.copy(alpha = 0.1f),
                    selectedContainerColor = Color(0xFFE040FB),
                    disabledContainerColor = Color.White.copy(alpha = 0.05f)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = enabled,
                    selected = isSelected,
                    borderColor = Color(0xFFE040FB),
                    disabledBorderColor = Color.Transparent
                )
            )
        }
    }
}