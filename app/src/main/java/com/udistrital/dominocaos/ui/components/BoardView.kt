package com.udistrital.dominocaos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udistrital.dominocaos.domain.Side
import com.udistrital.dominocaos.domain.Tile

@Composable
fun BoardView(
    board: List<Tile>,
    modifier: Modifier = Modifier,
    validSides: List<Side> = emptyList(),
    selectedSide: Side? = null
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(board.size) {
        if (board.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    val leftEnd = board.firstOrNull()?.a
    val rightEnd = board.lastOrNull()?.b

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (board.isEmpty()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🁣 Tablero vacío", color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Selecciona una ficha de tu mano para iniciar", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Indicador de Extremo Izquierdo
                leftEnd?.let { pip ->
                    val isHighlight = Side.LEFT in validSides
                    val isSelected = selectedSide == Side.LEFT
                    Surface(
                        color = when {
                            isSelected -> Color(0xFFE040FB)
                            isHighlight -> Color(0xFF00E5FF).copy(alpha = 0.3f)
                            else -> Color.White.copy(alpha = 0.1f)
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = if (isHighlight || isSelected) BorderStroke(1.5.dp, if (isSelected) Color(0xFFE040FB) else Color(0xFF00E5FF)) else null
                    ) {
                        Text(
                            text = "◀ $pip",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }

                // Renderizado de las fichas del tablero (dobles en vertical)
                board.forEach { tile ->
                    DominoTile(
                        tile = tile,
                        cell = 32.dp,
                        vertical = tile.isDouble
                    )
                }

                // Indicador de Extremo Derecho
                rightEnd?.let { pip ->
                    val isHighlight = Side.RIGHT in validSides
                    val isSelected = selectedSide == Side.RIGHT
                    Surface(
                        color = when {
                            isSelected -> Color(0xFFE040FB)
                            isHighlight -> Color(0xFF00E5FF).copy(alpha = 0.3f)
                            else -> Color.White.copy(alpha = 0.1f)
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = if (isHighlight || isSelected) BorderStroke(1.5.dp, if (isSelected) Color(0xFFE040FB) else Color(0xFF00E5FF)) else null
                    ) {
                        Text(
                            text = "$pip ▶",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}