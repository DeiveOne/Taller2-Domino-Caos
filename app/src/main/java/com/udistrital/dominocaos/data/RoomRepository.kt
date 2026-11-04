package com.udistrital.dominocaos.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.udistrital.dominocaos.domain.GameEngine
import com.udistrital.dominocaos.domain.GameState
import com.udistrital.dominocaos.domain.Status
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Sincronización en tiempo real: la sala es UN documento `rooms/{code}`.
 * El estado de juego viaja como JSON en el campo `state`; cada jugada se aplica
 * dentro de una transacción de Firestore para evitar jugadas simultáneas.
 */
class RoomRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    private val rooms = db.collection("rooms")

    fun observe(code: String): Flow<Room?> = callbackFlow {
        val reg = rooms.document(code).addSnapshotListener { snap, err ->
            if (err != null) close(err) else trySend(snap?.takeIf { it.exists() }?.toRoom())
        }
        awaitClose { reg.remove() }
    }

    suspend fun create(host: LobbyPlayer, maxRounds: Int): String {
        val code = (1..5).map { CODE_CHARS.random() }.joinToString("")
        rooms.document(code).set(
            mapOf(
                "hostUid" to host.uid, "status" to Status.WAITING.name, "maxRounds" to maxRounds,
                "lobby" to listOf(host.toMap()), "state" to null
            )
        ).await()
        return code
    }

    suspend fun join(code: String, user: LobbyPlayer) = transact(code) { room ->
        check(room.status == Status.WAITING) { "La partida ya comenzó" }
        check(room.lobby.size < 4) { "La sala está llena" }
        if (room.lobby.any { it.uid == user.uid }) emptyMap()
        else mapOf("lobby" to (room.lobby + user).map { it.toMap() })
    }

    /** Inicia o reinicia la partida (solo anfitrión). */
    suspend fun start(code: String, uid: String) = transact(code) { room ->
        check(room.hostUid == uid) { "Solo el anfitrión puede iniciar" }
        check(room.lobby.size >= 2) { "Se necesitan al menos 2 jugadores" }
        check(room.status != Status.PLAYING) { "La partida ya está en curso" }
        val g = GameEngine.newGame(room.lobby.map { it.uid to it.name }, room.maxRounds)
        mapOf("status" to g.status.name, "state" to GameJson.encode(g))
    }

    /** Revancha rápida al finalizar la partida. */
    suspend fun rematch(code: String, uid: String) = start(code, uid)

    suspend fun mutate(code: String, f: (GameState) -> Result<GameState>) = transact(code) { room ->
        val g = f(checkNotNull(room.state) { "La partida no ha iniciado" }).getOrThrow()
        if (g.status == Status.FINISHED && room.state.status != Status.FINISHED) {
            updateStats(g)
        }
        mapOf("status" to g.status.name, "state" to GameJson.encode(g))
    }

    private fun updateStats(g: GameState) {
        g.players.forEach { p ->
            val ref = db.collection("users").document(p.uid)
            db.runTransaction { tx ->
                val snap = tx.get(ref)
                val currentPlayed = snap.getLong("gamesPlayed") ?: 0L
                val currentWon = snap.getLong("gamesWon") ?: 0L
                val currentScore = snap.getLong("totalScore") ?: 0L
                val isWinner = p.uid == g.winnerUid
                tx.set(
                    ref,
                    mapOf(
                        "name" to p.name,
                        "gamesPlayed" to currentPlayed + 1,
                        "gamesWon" to currentWon + (if (isWinner) 1 else 0),
                        "totalScore" to currentScore + p.score
                    ),
                    SetOptions.merge()
                )
                null
            }
        }
    }

    suspend fun leave(code: String, uid: String) = transact(code) { room ->
        when (room.status) {
            Status.WAITING -> mapOf("lobby" to room.lobby.filter { it.uid != uid }.map { it.toMap() })
            Status.PLAYING -> GameEngine.abandon(room.state!!, uid).let { mapOf("status" to it.status.name, "state" to GameJson.encode(it)) }
            Status.FINISHED -> emptyMap()
        }
    }

    private suspend fun transact(code: String, build: (Room) -> Map<String, Any?>) {
        db.runTransaction { tx ->
            val ref = rooms.document(code)
            val snap = tx.get(ref)
            check(snap.exists()) { "La sala no existe" }
            val updates = build(snap.toRoom())
            if (updates.isNotEmpty()) tx.update(ref, updates)
            null
        }.await()
    }

    private fun DocumentSnapshot.toRoom(): Room {
        @Suppress("UNCHECKED_CAST")
        val lobby = (get("lobby") as? List<Map<String, String>>).orEmpty()
            .map { LobbyPlayer(it["uid"].orEmpty(), it["name"].orEmpty()) }
        return Room(
            code = id, hostUid = getString("hostUid").orEmpty(),
            status = Status.valueOf(getString("status") ?: Status.WAITING.name),
            lobby = lobby, maxRounds = (getLong("maxRounds") ?: 10L).toInt(),
            state = getString("state")?.let(GameJson::decode)
        )
    }

    private companion object { const val CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" }
}
