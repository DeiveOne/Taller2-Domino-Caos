package com.udistrital.dominocaos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.udistrital.dominocaos.domain.Tile

@Composable
fun HandView(hand: List<Tile>, selected: Tile?, enabled: Boolean, onSelect: (Tile) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
        items(hand) { t ->
            DominoTile(
                t, selected = t == selected, modifier = Modifier.alpha(if (enabled) 1f else 0.6f),
                onClick = if (enabled) ({ onSelect(t) }) else null
            )
        }
    }
}
