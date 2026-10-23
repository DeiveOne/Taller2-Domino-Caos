package com.udistrital.dominocaos.domain

import kotlin.random.Random

/** Lógica pura del juego (sin Android ni Firebase): fácil de probar con JUnit. */
object GameEngine {
    const val HAND_SIZE = 7
    const val EVENT_EVERY = 3          // cada 3 turnos hay oportunidad de evento
    const val EVENT_CHANCE = 60        // % de probabilidad
    const val EMPTY_HAND_BONUS = 10

    fun fullSet() = (0..6).flatMap { a -> (a..6).map { b -> Tile(a, b) } }

    fun newGame(players: List<Pair<String, String>>, maxRounds: Int = 10, rng: Random = Random): GameState {
        require(players.size in 2..4) { "La partida admite de 2 a 4 jugadores" }
        val deck = fullSet().shuffled(rng)
        val ps = players.mapIndexed { i, (uid, name) ->
            PlayerState(uid, name, deck.subList(i * HAND_SIZE, (i + 1) * HAND_SIZE).toList())
        }
        return GameState(ps, pool = deck.drop(players.size * HAND_SIZE), maxRounds = maxRounds, log = "¡Comienza la partida!")
    }

    // ---------- Validación ----------
    fun sides(s: GameState, t: Tile, wildcard: Boolean = false): List<Side> {
        if (s.board.isEmpty()) return listOf(Side.RIGHT)
        val l = s.board.first().a
        val r = s.board.last().b
        fun fits(e: Int) = t.a == e || t.b == e
        return buildList {
            if (wildcard || fits(l)) add(Side.LEFT)
            if (wildcard || fits(r)) add(Side.RIGHT)
        }
    }

    fun hasMove(s: GameState, p: PlayerState) = p.hand.any { sides(s, it).isNotEmpty() }

    private fun canAct(s: GameState, p: PlayerState) =
        hasMove(s, p) || s.pool.isNotEmpty() || (!s.powersFrozen && (p.powers[Power.WILDCARD] ?: 0) > 0)

    // ---------- Acciones del jugador ----------
    fun play(s: GameState, uid: String, m: Move, rng: Random = Random): Result<GameState> = runCatching {
        val me = checkTurn(s, uid)
        val power = m.power
        if (power != null) {
            require(!s.powersFrozen) { "Los poderes están congelados" }
            require((me.powers[power] ?: 0) > 0) { "No te quedan usos de ${power.label}" }
        }
        val used = if (power == null) me else me.copy(
            powers = me.powers + (power to ((me.powers[power] ?: 0) - 1)),
            powersUsed = me.powersUsed + 1
        )
        if (power == Power.SWAP) return@runCatching swap(s, used, m, rng)

        val tile = requireNotNull(m.tile) { "Selecciona una ficha" }
        val side = requireNotNull(m.side) { "Selecciona un lado" }
        require(tile in me.hand) { "No tienes esa ficha" }
        require(side in sides(s, tile, power == Power.WILDCARD)) { "Movimiento inválido" }

        val placed = orient(s, tile, side)
        var pts = tile.pips + if (tile.isDouble) 2 else 0
        if (s.activeEvent == EventType.DOUBLE_POINTS) pts *= 2
        val hand = me.hand - tile
        val bonus = if (hand.isEmpty()) EMPTY_HAND_BONUS else 0
        val st = s.copy(
            board = if (side == Side.LEFT) listOf(placed) + s.board else s.board + placed,
            players = s.players.replace(s.currentIdx, used.copy(hand = hand, score = used.score + pts + bonus)),
            direction = if (power == Power.REVERSE) -s.direction else s.direction,
            consecutivePasses = 0,
            log = "${me.name} jugó [${tile.a}|${tile.b}] (+$pts)"
        )
        if (hand.isEmpty()) finish(st, "${me.name} se quedó sin fichas")
        else advance(st, if (power == Power.SKIP) 1 else 0, rng)
    }

    fun draw(s: GameState, uid: String, rng: Random = Random): Result<GameState> = runCatching {
        val me = checkTurn(s, uid)
        require(!hasMove(s, me)) { "Tienes un movimiento válido" }
        require(s.pool.isNotEmpty()) { "El pozo está vacío" }
        settle(
            s.copy(
                pool = s.pool.drop(1),
                players = s.players.replace(s.currentIdx, me.copy(hand = me.hand + s.pool.first())),
                consecutivePasses = 0,
                log = "${me.name} tomó una ficha del pozo"
            ), rng
        )
    }

    fun pass(s: GameState, uid: String, rng: Random = Random): Result<GameState> = runCatching {
        val me = checkTurn(s, uid)
        require(!hasMove(s, me) && s.pool.isEmpty()) { "No puedes pasar" }
        passTurn(s.copy(log = "${me.name} pasó el turno"), rng)
    }

    fun abandon(s: GameState, uid: String): GameState {
        if (s.status != Status.PLAYING) return s
        val name = s.players.firstOrNull { it.uid == uid }?.name ?: "Un jugador"
        val winner = s.players.filter { it.uid != uid }.maxByOrNull { it.score }
        return s.copy(status = Status.FINISHED, winnerUid = winner?.uid, log = "$name abandonó la partida")
    }

    // ---------- Reacciones ----------
    fun react(s: GameState, uid: String, emoji: String): Result<GameState> = runCatching {
        require(s.status == Status.PLAYING) { "La partida no está activa" }
        val idx = s.players.indexOfFirst { it.uid == uid }
        require(idx >= 0) { "Jugador no encontrado" }

        val p = s.players[idx]
        val newReaction = PlayerReaction(emoji, System.currentTimeMillis())

        s.copy(players = s.players.replace(idx, p.copy(reaction = newReaction)))
    }

    // ---------- Fin de partida ----------
    fun finish(s: GameState, reason: String): GameState {
        val sorted = s.players.sortedWith(compareByDescending<PlayerState> { it.score }.thenBy { it.powersUsed })
        val tie = sorted.size > 1 && sorted[0].score == sorted[1].score && sorted[0].powersUsed == sorted[1].powersUsed
        return s.copy(status = Status.FINISHED, winnerUid = if (tie) null else sorted[0].uid, log = reason)
    }

    // ---------- Eventos ----------
    fun applyEvent(s: GameState, e: EventType): GameState {
        val n = s.players.size
        val next = ((s.currentIdx + s.direction) % n + n) % n
        val base = s.copy(log = s.log + " · \u26A1 ${e.label}")
        return when (e) {
            EventType.DOUBLE_POINTS, EventType.POWERS_FROZEN -> base.copy(activeEvent = e, eventTurns = 3)
            EventType.REVERSE_DIRECTION -> base.copy(direction = -s.direction)
            EventType.SKIP_NEXT -> base.copy(skipPending = true)
            EventType.DRAW_PENALTY ->
                if (s.pool.isEmpty()) base
                else base.copy(pool = s.pool.drop(1), players = s.players.replace(next, s.players[next].let { it.copy(hand = it.hand + s.pool.first()) }))
            EventType.ROTATE_HANDS ->
                base.copy(players = s.players.mapIndexed { i, p -> p.copy(hand = s.players[((i - s.direction) % n + n) % n].hand) })
            EventType.POOL_SHRINK -> base.copy(pool = s.pool.drop(2))
        }
    }

    // ---------- Internos ----------
    private fun checkTurn(s: GameState, uid: String): PlayerState {
        require(s.status == Status.PLAYING) { "La partida no está activa" }
        require(s.current.uid == uid) { "No es tu turno" }
        return s.current
    }

    private fun orient(s: GameState, t: Tile, side: Side): Tile {
        if (s.board.isEmpty()) return t
        return if (side == Side.LEFT) {
            val e = s.board.first().a
            when { t.b == e -> t; t.a == e -> t.flip(); else -> t }
        } else {
            val e = s.board.last().b
            when { t.a == e -> t; t.b == e -> t.flip(); else -> t }
        }
    }

    private fun swap(s: GameState, me: PlayerState, m: Move, rng: Random): GameState {
        val mine = requireNotNull(m.tile) { "Selecciona tu ficha" }
        require(mine in me.hand) { "No tienes esa ficha" }
        val oppIdx = s.players.indexOfFirst { it.uid == m.swapTargetUid }
        require(oppIdx >= 0 && oppIdx != s.currentIdx) { "Elige un oponente" }
        val opp = s.players[oppIdx]
        require(opp.hand.isNotEmpty()) { "El oponente no tiene fichas" }
        val theirs = opp.hand.random(rng)
        val players = s.players.mapIndexed { i, p ->
            when (i) {
                s.currentIdx -> me.copy(hand = me.hand - mine + theirs)
                oppIdx -> p.copy(hand = p.hand - theirs + mine)
                else -> p
            }
        }
        return advance(s.copy(players = players, consecutivePasses = 0, log = "${me.name} intercambió una ficha con ${opp.name}"), 0, rng)
    }

    private fun passTurn(s: GameState, rng: Random): GameState {
        val st = s.copy(consecutivePasses = s.consecutivePasses + 1)
        return if (st.consecutivePasses >= st.players.size) finish(st, "Partida bloqueada: nadie puede jugar")
        else advance(st, 0, rng)
    }

    /** Si el jugador de turno no puede hacer nada (sin pozo ni comodín) pierde el turno automáticamente. */
    private fun settle(s: GameState, rng: Random): GameState =
        if (canAct(s, s.current)) s else passTurn(s.copy(log = s.log + " · ${s.current.name} no puede jugar"), rng)

    private fun advance(s: GameState, skips: Int, rng: Random): GameState {
        var st = s.copy(turnCount = s.turnCount + 1)
        if (st.eventTurns > 0) {
            val left = st.eventTurns - 1
            st = st.copy(eventTurns = left, activeEvent = if (left == 0) null else st.activeEvent)
        }
        if (st.turnCount % EVENT_EVERY == 0 && rng.nextInt(100) < EVENT_CHANCE)
            st = applyEvent(st, EventType.entries.random(rng))
        if (st.turnCount / st.players.size >= st.maxRounds) return finish(st, "Se alcanzó el máximo de rondas")
        val n = st.players.size
        val steps = 1 + skips + if (st.skipPending) 1 else 0
        st = st.copy(skipPending = false, currentIdx = ((st.currentIdx + st.direction * steps) % n + n) % n)
        return settle(st, rng)
    }

    private fun <T> List<T>.replace(i: Int, v: T) = mapIndexed { j, x -> if (j == i) v else x }
}