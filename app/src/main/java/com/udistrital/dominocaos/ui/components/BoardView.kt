package com.udistrital.dominocaos.ui.components

import androidx.compose.foundation.background
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

/** Tablero en tiempo real: se actualiza con cada snapshot de Firestore. */
@Composable
fun BoardView(board: List<Tile>, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    LaunchedEffect(board.size) { if (board.isNotEmpty()) listState.animateScrollToItem(board.lastIndex) }
    Box(
        modifier.fillMaxWidth().height(90.dp).background(Color(0xFF2E7D32), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (board.isEmpty()) Text("Tablero vacío: juega la primera ficha", color = Color.White)
        else LazyRow(
            state = listState, horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(8.dp), verticalAlignment = Alignment.CenterVertically
        ) { items(board) { DominoTile(it, cell = 30.dp) } }
    }
}
