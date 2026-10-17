package com.udistrital.dominocaos.data

import com.udistrital.dominocaos.domain.GameState
import com.udistrital.dominocaos.domain.Status
import kotlinx.serialization.json.Json

data class LobbyPlayer(val uid: String, val name: String) {
    fun toMap() = mapOf("uid" to uid, "name" to name)
}

data class Room(
    val code: String, val hostUid: String, val status: Status,
    val lobby: List<LobbyPlayer>, val maxRounds: Int, val state: GameState?
)

object GameJson {
    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }
    fun encode(s: GameState): String = json.encodeToString(GameState.serializer(), s)
    fun decode(t: String): GameState = json.decodeFromString(GameState.serializer(), t)
}
