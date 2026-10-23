package com.udistrital.dominocaos.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    // Fondo oscuro Neon/Synthwave para toda la pantalla
    val background = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF120320),
            Color(0xFF2E094E),
            Color(0xFF5E14A7)
        )
    )

    if (r == null) {
        Box(Modifier.fillMaxSize().background(background), Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFE040FB))
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val s = r.state
            if (r.status == Status.WAITING || s == null) {
                WaitingView(r, vm, onExit)
            } else {
                GameView(r, s, vm, onExit)
            }
        }
    }
}

@Composable
private fun WaitingView(r: Room, vm: GameViewModel, onExit: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SALA DE ESPERA",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Tarjeta grande para el código de la sala
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("CÓDIGO DE INVITACIÓN", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                Text(
                    text = r.code,
                    color = Color(0xFF00E5FF), // Cyan vibrante
                    fontSize = 56.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 8.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Partida a ${r.maxRounds} rondas", color = Color.White.copy(alpha = 0.8f))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Lista de Jugadores
        Text(
            text = "JUGADORES (${r.lobby.size}/4)",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(12.dp))

        r.lobby.forEach { player ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = player.name,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    if (player.uid == r.hostUid) {
                        Icon(Icons.Default.Star, contentDescription = "Anfitrión", tint = Color(0xFFFFD54F))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        vm.message?.let { Text(it, color = Color(0xFFFF5252)) }

        Spacer(modifier = Modifier.weight(1f))

        // Botón de acción principal
        if (vm.myUid == r.hostUid) {
            Button(
                onClick = { vm.start() },
                enabled = r.lobby.size >= 2,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE040FB),
                    disabledContainerColor = Color(0xFFE040FB).copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("INICIAR PARTIDA", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            if (r.lobby.size < 2) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Esperando a que se unan más jugadores...", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            }
        } else {
            Text("Esperando a que el anfitrión inicie la partida...", color = Color(0xFF00E5FF), fontStyle = FontStyle.Italic)
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = { vm.leave(onExit) }) {
            Text("SALIR DE LA SALA", color = Color.White.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun GameView(room: Room, s: GameState, vm: GameViewModel, onExit: () -> Unit) {
    val me = s.players.firstOrNull { it.uid == vm.myUid }
    val playing = s.status == Status.PLAYING
    val myTurn = playing && s.current.uid == vm.myUid
    val swapMode = myTurn && vm.selectedPower == Power.SWAP && vm.selectedTile != null

    var showHelp by remember { mutableStateOf(false) }

    // Instanciamos el Vibrator nativo de Android
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }

    val vibrate = { durationMillis: Long ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMillis)
        }
    }

    LaunchedEffect(myTurn) {
        if (myTurn) vibrate(200L)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Sala ${room.code} · Ronda ${s.round}/${s.maxRounds}\nSentido ${if (s.direction == 1) "➡" else "⬅"} · Pozo ${s.pool.size}",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { showHelp = true }) {
            Icon(Icons.Default.Info, contentDescription = "Ver reglas", tint = Color.White)
        }
    }

    s.activeEvent?.let { Text("⚡ Evento activo: ${it.label} (${s.eventTurns})", color = Color(0xFFE040FB), fontWeight = FontWeight.Bold) }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(s.players) { p ->
            PlayerCard(
                p, isTurn = playing && p.uid == s.current.uid, isMe = p.uid == vm.myUid,
                onClick = if (swapMode && p.uid != vm.myUid) {
                    {
                        vibrate(100L)
                        vm.swapWith(p.uid)
                    }
                } else null
            )
        }
    }

    Text(
        text = when {
            !playing -> s.winnerUid?.let { id -> "🏆 Ganó ${s.players.first { it.uid == id }.name}" } ?: "🤝 Empate"
            myTurn -> "Tu turno"
            else -> "Turno de ${s.current.name}"
        },
        style = MaterialTheme.typography.titleMedium,
        color = Color(0xFF00E5FF),
        fontWeight = FontWeight.Bold
    )

    BoardView(s.board)

    if (s.log.isNotBlank()) Text(s.log, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
    if (swapMode) Text("Intercambio: toca la tarjeta de un oponente", color = Color(0xFFE040FB), fontWeight = FontWeight.Bold)
    vm.message?.let { Text(it, color = Color(0xFFFF5252)) }

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
                        vibrate(30L)
                        vm.sendReaction(emoji)
                    }
                )
            }
        }

        PowerBar(me, vm.selectedPower, enabled = myTurn && !s.powersFrozen) {
            vibrate(30L)
            vm.selectPower(it)
        }

        ControlsBar(
            sides = if (myTurn) vm.validSides(s) else emptyList(),
            canDraw = myTurn && noMove && s.pool.isNotEmpty(),
            canPass = myTurn && noMove && s.pool.isEmpty(),
            poolSize = s.pool.size,
            onSide = { side ->
                vibrate(100L)
                vm.place(side)
            },
            onDraw = {
                vibrate(30L)
                vm.draw()
            },
            onPass = {
                vibrate(30L)
                vm.pass()
            }
        )

        HandView(me.hand, vm.selectedTile, enabled = myTurn, onSelect = { tile ->
            vibrate(30L)
            vm.selectTile(tile)
        })

        OutlinedButton(
            onClick = { vm.leave(onExit) },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = null
        ) { Text("Abandonar partida", color = Color.White.copy(alpha = 0.7f)) }
    }

    if (!playing) {
        if (vm.myUid == room.hostUid) {
            Button(
                onClick = { vm.start() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE040FB))
            ) { Text("Reiniciar partida") }
        }
        OutlinedButton(
            onClick = { vm.leave(onExit) },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = null
        ) { Text("Salir", color = Color.White) }
    }

    if (showHelp) {
        HelpDialog(onDismiss = { showHelp = false })
    }
}