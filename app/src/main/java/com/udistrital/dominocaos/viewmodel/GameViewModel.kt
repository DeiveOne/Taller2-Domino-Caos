package com.udistrital.dominocaos.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udistrital.dominocaos.data.AuthRepository
import com.udistrital.dominocaos.data.RoomRepository
import com.udistrital.dominocaos.domain.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Conecta la UI con el motor (dominio) y con Firestore (datos). No contiene reglas del juego. */
class GameViewModel(
    private val code: String,
    auth: AuthRepository = AuthRepository(),
    private val rooms: RoomRepository = RoomRepository()
) : ViewModel() {
    val myUid = auth.current()?.uid.orEmpty()

    var message by mutableStateOf<String?>(null); private set
    var selectedTile by mutableStateOf<Tile?>(null); private set
    var selectedPower by mutableStateOf<Power?>(null); private set
    var timerSeconds by mutableStateOf(30); private set

    private var timerJob: Job? = null
    private var lastTurnKey: String = ""

    val room = rooms.observe(code)
        .onEach { r -> updateTimerForRoom(r?.state) }
        .catch { message = it.localizedMessage }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private fun updateTimerForRoom(s: GameState?) {
        if (s == null || s.status != Status.PLAYING) {
            timerJob?.cancel()
            timerSeconds = 30
            return
        }
        val currentTurnKey = "${s.turnCount}_${s.current.uid}"
        if (currentTurnKey != lastTurnKey) {
            lastTurnKey = currentTurnKey
            timerJob?.cancel()
            timerSeconds = 30
            timerJob = viewModelScope.launch {
                while (timerSeconds > 0) {
                    delay(1000L)
                    timerSeconds--
                }
                if (s.current.uid == myUid && s.status == Status.PLAYING) {
                    autoPlayTimeout(s)
                }
            }
        }
    }

    private fun autoPlayTimeout(s: GameState) {
        val me = s.players.firstOrNull { it.uid == myUid } ?: return
        val playableTile = me.hand.firstOrNull { t -> GameEngine.sides(s, t, false).isNotEmpty() }
        if (playableTile != null) {
            val side = GameEngine.sides(s, playableTile, false).first()
            selectedTile = playableTile
            place(side)
        } else if (s.pool.isNotEmpty()) {
            draw()
        } else {
            pass()
        }
    }

    fun selectTile(t: Tile) { selectedTile = if (selectedTile == t) null else t }
    fun selectPower(p: Power) { selectedPower = if (selectedPower == p) null else p }

    /** Lados donde puede colocarse la ficha elegida (considera el Comodín si está activo). */
    fun validSides(s: GameState): List<Side> {
        val t = selectedTile ?: return emptyList()
        if (selectedPower == Power.SWAP) return emptyList()
        return GameEngine.sides(s, t, selectedPower == Power.WILDCARD)
    }

    fun place(side: Side) = act { GameEngine.play(it, myUid, Move(selectedTile, side, selectedPower)) }
    fun swapWith(uid: String) = act { GameEngine.play(it, myUid, Move(selectedTile, null, Power.SWAP, uid)) }
    fun draw() = act { GameEngine.draw(it, myUid) }
    fun pass() = act { GameEngine.pass(it, myUid) }

    // NUEVA FUNCIÓN: Envía la reacción a Firestore
    fun sendReaction(emoji: String) = act { GameEngine.react(it, myUid, emoji) }

    fun start() = launchSafe { rooms.start(code, myUid) }      // iniciar o reiniciar
    fun rematch() = launchSafe { rooms.rematch(code, myUid) }  // revancha rápida
    fun leave(onDone: () -> Unit) = launchSafe { rooms.leave(code, myUid); onDone() }

    private fun act(f: (GameState) -> Result<GameState>) {
        val current = room.value?.state ?: return
        f(current).onFailure { message = it.localizedMessage; return }   // validación local rápida
        launchSafe {
            rooms.mutate(code, f)
            selectedTile = null; selectedPower = null
        }
    }

    private fun launchSafe(block: suspend () -> Unit) {
        message = null
        viewModelScope.launch { runCatching { block() }.onFailure { message = it.localizedMessage } }
    }
}