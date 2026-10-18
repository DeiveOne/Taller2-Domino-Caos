package com.udistrital.dominocaos.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.ui.platform.LocalContext
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

    var showHelp by remember { mutableStateOf(false) }

    // 1. Instanciamos el Vibrator nativo de Android
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }

    // Función auxiliar para emitir la vibración
    val vibrate = { durationMillis: Long ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMillis)
        }
    }

    // 2. Efecto para vibrar fuerte (200ms) cuando empieza tu turno
    LaunchedEffect(myTurn) {
        if (myTurn) vibrate(200L)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Sala ${room.code} · Ronda ${s.round}/${s.maxRounds}\nSentido ${if (s.direction == 1) "➡" else "⬅"} · Pozo ${s.pool.size}",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { showHelp = true }) {
            Icon(Icons.Default.Info, contentDescription = "Ver reglas y poderes")
        }
    }

    s.activeEvent?.let { Text("⚡ Evento activo: ${it.label} (${s.eventTurns})", color = Color(0xFF6A1B9A)) }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(s.players) { p ->
            PlayerCard(
                p, isTurn = playing && p.uid == s.current.uid, isMe = p.uid == vm.myUid,
                onClick = if (swapMode && p.uid != vm.myUid) {
                    {
                        vibrate(100L) // Vibración media al intercambiar ficha
                        vm.swapWith(p.uid)
                    }
                } else null
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

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            listOf("😂", "😡", "👏", "🔥", "🤔", "⚡").forEach { emoji ->
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.clickable {
                        vibrate(30L) // Vibración suave al enviar emoji
                        vm.sendReaction(emoji)
                    }
                )
            }
        }

        PowerBar(me, vm.selectedPower, enabled = myTurn && !s.powersFrozen) {
            vibrate(30L) // Vibración suave al seleccionar poder
            vm.selectPower(it)
        }

        ControlsBar(
            sides = if (myTurn) vm.validSides(s) else emptyList(),
            canDraw = myTurn && noMove && s.pool.isNotEmpty(),
            canPass = myTurn && noMove && s.pool.isEmpty(),
            poolSize = s.pool.size,
            onSide = { side ->
                vibrate(100L) // Vibración media al colocar ficha en el tablero
                vm.place(side)
            },
            onDraw = {
                vibrate(30L) // Vibración suave al robar
                vm.draw()
            },
            onPass = {
                vibrate(30L) // Vibración suave al pasar
                vm.pass()
            }
        )

        HandView(me.hand, vm.selectedTile, enabled = myTurn, onSelect = { tile ->
            vibrate(30L) // Vibración suave al tocar ficha de la mano
            vm.selectTile(tile)
        })

        OutlinedButton({ vm.leave(onExit) }) { Text("Abandonar partida") }
    }

    if (!playing) {
        if (vm.myUid == room.hostUid) Button({ vm.start() }) { Text("Reiniciar partida") }
        OutlinedButton({ vm.leave(onExit) }) { Text("Salir") }
    }

    if (showHelp) {
        HelpDialog(onDismiss = { showHelp = false })
    }
}