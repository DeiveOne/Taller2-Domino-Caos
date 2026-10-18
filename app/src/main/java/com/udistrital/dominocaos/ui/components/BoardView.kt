package com.udistrital.dominocaos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.udistrital.dominocaos.domain.Tile

@Composable
fun BoardView(board: List<Tile>, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    LaunchedEffect(board.size) { if (board.isNotEmpty()) listState.animateScrollToItem(board.lastIndex) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (board.isEmpty()) {
            Text("Tablero vacío: Juega la primera ficha", color = Color.White.copy(alpha = 0.6f))
        } else {
            LazyRow(
                state = listState, horizontalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(12.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                items(board) { DominoTile(it, cell = 36.dp) }
            }
        }
    }
}