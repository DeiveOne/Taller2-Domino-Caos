package com.udistrital.dominocaos.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.udistrital.dominocaos.domain.Tile

private fun pipPositions(n: Int): List<Pair<Float, Float>> {
    val l = .25f; val m = .5f; val r = .75f
    return when (n) {
        0 -> emptyList()
        1 -> listOf(m to m)
        2 -> listOf(l to l, r to r)
        3 -> listOf(l to l, m to m, r to r)
        4 -> listOf(l to l, r to l, l to r, r to r)
        5 -> listOf(l to l, r to l, m to m, l to r, r to r)
        else -> listOf(l to l, r to l, l to m, r to m, l to r, r to r)
    }
}

@Composable
private fun Half(value: Int, cell: Dp, dotColor: Color) {
    Canvas(Modifier.size(cell)) {
        pipPositions(value).forEach { (x, y) ->
            drawCircle(
                color = dotColor,
                radius = size.minDimension * 0.12f, // Puntos ligeramente más grandes
                center = Offset(x * size.width, y * size.height)
            )
        }
    }
}

@Composable
fun DominoTile(
    tile: Tile, modifier: Modifier = Modifier, cell: Dp = 40.dp,
    selected: Boolean = false, onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(8.dp)

    // Fichas en modo oscuro
    val tileColor = Color(0xFF1E1E2E)
    val dotColor = Color(0xFF00E5FF) // Puntos Cyan brillante
    val borderColor = if (selected) Color(0xFFE040FB) else Color.White.copy(alpha = 0.2f)

    Row(
        modifier = modifier
            .clip(shape)
            .background(tileColor)
            .border(if (selected) 3.dp else 1.dp, borderColor, shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Half(tile.a, cell, dotColor)
        Box(Modifier.width(1.dp).height(cell).background(Color.White.copy(alpha = 0.2f))) // Línea divisoria
        Half(tile.b, cell, dotColor)
    }
}