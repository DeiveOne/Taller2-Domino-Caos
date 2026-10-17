package com.udistrital.dominocaos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.udistrital.dominocaos.data.Room
import com.udistrital.dominocaos.domain.*
import com.udistrital.dominocaos.ui.components.*
import com.udistrital.dominocaos.viewmodel.GameViewModel

@Composable
fun RoomScreen(code: String, onExit: () -> Unit) {
    val vm: GameViewModel = viewModel(key = code, factory = viewModelFactory { initializer { GameViewModel(code) } })
    val room by vm.room.collectAsState()
    val r = room
    if (r == null) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        return
    }
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val s = r.state
        if (r.status == Status.WAITING || s == null) WaitingView(r, vm, onExit) else GameView(r, s, vm, onExit)
    }
}

@Composable
private fun WaitingView(r: Room, vm: GameViewModel, onExit: () -> Unit) {
    Text("Sala ${r.code}", style = MaterialTheme.typography.headlineMedium)
    Text("Comparte el código con tus amigos. Rondas máximas: ${r.maxRounds}")
    r.lobby.forEach { Text("• ${it.name}" + if (it.uid == r.hostUid) " (anfitrión)" else "") }
    vm.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    if (vm.myUid == r.hostUid) Button({ vm.start() }, enabled = r.lobby.size >= 2) { Text("Iniciar partida") }
    else Text("Esperando al anfitrión…")
    OutlinedButton({ vm.leave(onExit) }) { Text("Salir de la sala") }
}

@Composable
private fun GameView(room: Room, s: GameState, vm: GameViewModel, onExit: () -> Unit) {
    val me = s.players.firstOrNull { it.uid == vm.myUid }
    val playing = s.status == Status.PLAYING
    val myTurn = playing && s.current.uid == vm.myUid
    val swapMode = myTurn && vm.selectedPower == Power.SWAP && vm.selectedTile != null

    Text(
        "Sala ${room.code} · Ronda ${s.round}/${s.maxRounds} · Sentido ${if (s.direction == 1) "➡" else "⬅"} · Pozo ${s.pool.size}",
        fontWeight = FontWeight.Bold
    )
    s.activeEvent?.let { Text("⚡ Evento activo: ${it.label} (${s.eventTurns})", color = Color(0xFF6A1B9A)) }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(s.players) { p ->
            PlayerCard(
                p, isTurn = playing && p.uid == s.current.uid, isMe = p.uid == vm.myUid,
                onClick = if (swapMode && p.uid != vm.myUid) ({ vm.swapWith(p.uid) }) else null
            )
        }
    }

    Text(
        when {
            !playing -> s.winnerUid?.let { id -> "🏆 Ganó ${s.players.first { it.uid == id }.name}" } ?: "🤝 Empate"
            myTurn -> "Tu turno"
            else -> "Turno de ${s.current.name}"
        },
        style = MaterialTheme.typography.titleMedium
    )
    BoardView(s.board)
    if (s.log.isNotBlank()) Text(s.log, style = MaterialTheme.typography.bodySmall)
    if (swapMode) Text("Intercambio: toca la tarjeta de un oponente", color = MaterialTheme.colorScheme.primary)
    vm.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }

    if (me != null && playing) {
        val noMove = !GameEngine.hasMove(s, me)
        PowerBar(me, vm.selectedPower, enabled = myTurn && !s.powersFrozen) { vm.selectPower(it) }
        ControlsBar(
            sides = if (myTurn) vm.validSides(s) else emptyList(),
            canDraw = myTurn && noMove && s.pool.isNotEmpty(),
            canPass = myTurn && noMove && s.pool.isEmpty(),
            poolSize = s.pool.size,
            onSide = vm::place, onDraw = vm::draw, onPass = vm::pass
        )
        HandView(me.hand, vm.selectedTile, enabled = myTurn, onSelect = vm::selectTile)
        OutlinedButton({ vm.leave(onExit) }) { Text("Abandonar partida") }
    }
    if (!playing) {
        if (vm.myUid == room.hostUid) Button({ vm.start() }) { Text("Reiniciar partida") }
        OutlinedButton({ vm.leave(onExit) }) { Text("Salir") }
    }
}
