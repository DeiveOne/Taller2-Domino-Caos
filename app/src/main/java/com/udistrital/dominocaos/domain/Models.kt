package com.udistrital.dominocaos.domain

import kotlinx.serialization.Serializable

@Serializable
data class Tile(val a: Int, val b: Int) {
    val pips get() = a + b
    val isDouble get() = a == b
    fun flip() = Tile(b, a)
}

enum class Side { LEFT, RIGHT }
enum class Status { WAITING, PLAYING, FINISHED }

enum class Power(val label: String, val initialUses: Int) {
    WILDCARD("Comodín", 2), SKIP("Salto", 1), SWAP("Intercambio", 1), REVERSE("Inversión", 1)
}

enum class EventType(val label: String) {
    DOUBLE_POINTS("Puntos dobles durante 3 turnos"),
    POWERS_FROZEN("Poderes congelados durante 3 turnos"),
    REVERSE_DIRECTION("Se invierte el sentido de los turnos"),
    SKIP_NEXT("El siguiente jugador pierde el turno"),
    DRAW_PENALTY("El siguiente jugador roba una ficha"),
    ROTATE_HANDS("Las manos rotan entre jugadores"),
    POOL_SHRINK("Se descartan 2 fichas del pozo")
}

@Serializable
data class PlayerReaction(val emoji: String, val timestamp: Long)

@Serializable
data class PlayerState(
    val uid: String,
    val name: String,
    val hand: List<Tile>,
    val score: Int = 0,
    val powers: Map<Power, Int> = Power.entries.associateWith { it.initialUses },
    val powersUsed: Int = 0,
    val reaction: PlayerReaction? = null
)

@Serializable
data class GameState(
    val players: List<PlayerState>,
    val board: List<Tile> = emptyList(),
    val pool: List<Tile>,
    val currentIdx: Int = 0,
    val direction: Int = 1,
    val turnCount: Int = 0,
    val maxRounds: Int = 10,
    val status: Status = Status.PLAYING,
    val activeEvent: EventType? = null,
    val eventTurns: Int = 0,
    val skipPending: Boolean = false,
    val consecutivePasses: Int = 0,
    val winnerUid: String? = null,
    val log: String = ""
) {
    val current get() = players[currentIdx]
    val round get() = minOf(turnCount / players.size + 1, maxRounds)
    val powersFrozen get() = activeEvent == EventType.POWERS_FROZEN
}

/** Acción de un jugador en su turno. `power` se elige ANTES de mover. */
data class Move(
    val tile: Tile? = null,
    val side: Side? = null,
    val power: Power? = null,
    val swapTargetUid: String? = null
)