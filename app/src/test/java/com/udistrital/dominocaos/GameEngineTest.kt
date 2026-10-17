package com.udistrital.dominocaos

import com.udistrital.dominocaos.domain.*
import org.junit.Assert.*
import org.junit.Test

class GameEngineTest {
    private fun t(a: Int, b: Int) = Tile(a, b)
    private fun game(vararg hands: List<Tile>, board: List<Tile> = emptyList(), pool: List<Tile> = emptyList()) =
        GameState(players = hands.mapIndexed { i, h -> PlayerState("p$i", "P$i", h) }, board = board, pool = pool)

    @Test fun newGameReparteSieteFichasYDejaPozo() {
        val g = GameEngine.newGame(listOf("a" to "A", "b" to "B"))
        assertTrue(g.players.all { it.hand.size == 7 })
        assertEquals(14, g.pool.size)
        assertEquals(28, (g.players.flatMap { it.hand } + g.pool).toSet().size)
    }

    @Test fun rechazaJugarFueraDeTurno() {
        val g = game(listOf(t(1, 1), t(2, 2)), listOf(t(3, 3)))
        assertTrue(GameEngine.play(g, "p1", Move(t(3, 3), Side.RIGHT)).isFailure)
    }

    @Test fun jugadaValidaSumaPuntosYPasaTurno() {
        val g = game(listOf(t(4, 5), t(0, 0)), listOf(t(5, 6)), board = listOf(t(3, 4)))
        val r = GameEngine.play(g, "p0", Move(t(4, 5), Side.RIGHT)).getOrThrow()
        assertEquals(listOf(t(3, 4), t(4, 5)), r.board)
        assertEquals(9, r.players[0].score)
        assertEquals(1, r.currentIdx)
    }

    @Test fun fichaOrientadaALaIzquierda() {
        val g = game(listOf(t(3, 2), t(0, 0)), listOf(t(3, 3)), board = listOf(t(3, 4)))
        val r = GameEngine.play(g, "p0", Move(t(3, 2), Side.LEFT)).getOrThrow()
        assertEquals(t(2, 3), r.board.first())
    }

    @Test fun rechazaMovimientoInvalido() {
        val g = game(listOf(t(0, 0), t(4, 4)), listOf(t(1, 1)), board = listOf(t(3, 4)))
        assertTrue(GameEngine.play(g, "p0", Move(t(0, 0), Side.RIGHT)).isFailure)
    }

    @Test fun comodinPermiteMovimientoYConsumeUso() {
        val g = game(listOf(t(0, 0), t(1, 1)), listOf(t(3, 3)), board = listOf(t(3, 4)))
        val r = GameEngine.play(g, "p0", Move(t(0, 0), Side.RIGHT, Power.WILDCARD)).getOrThrow()
        assertEquals(1, r.players[0].powers[Power.WILDCARD])
        assertEquals(1, r.players[0].powersUsed)
    }

    @Test fun poderesCongeladosSeRechazan() {
        val g = game(listOf(t(0, 0), t(1, 1)), listOf(t(3, 3)), board = listOf(t(3, 4)))
            .copy(activeEvent = EventType.POWERS_FROZEN, eventTurns = 3)
        assertTrue(GameEngine.play(g, "p0", Move(t(0, 0), Side.RIGHT, Power.WILDCARD)).isFailure)
    }

    @Test fun saltoHaceQueElSiguienteJugadorPierdaElTurno() {
        val g = game(listOf(t(4, 5), t(0, 0)), listOf(t(1, 1)), listOf(t(5, 6)), board = listOf(t(3, 4)))
        val r = GameEngine.play(g, "p0", Move(t(4, 5), Side.RIGHT, Power.SKIP)).getOrThrow()
        assertEquals(2, r.currentIdx)
    }

    @Test fun inversionCambiaElSentido() {
        val g = game(listOf(t(4, 5), t(0, 0)), listOf(t(1, 1)), listOf(t(5, 6)), board = listOf(t(3, 4)))
        val r = GameEngine.play(g, "p0", Move(t(4, 5), Side.RIGHT, Power.REVERSE)).getOrThrow()
        assertEquals(-1, r.direction)
        assertEquals(2, r.currentIdx)
    }

    @Test fun intercambioMueveFichasEntreJugadores() {
        val g = game(listOf(t(1, 2), t(3, 3)), listOf(t(5, 5)))
        val r = GameEngine.play(g, "p0", Move(t(1, 2), null, Power.SWAP, "p1")).getOrThrow()
        assertTrue(t(5, 5) in r.players[0].hand && t(1, 2) !in r.players[0].hand)
        assertTrue(t(1, 2) in r.players[1].hand)
        assertEquals(0, r.players[0].powers[Power.SWAP])
    }

    @Test fun robaDelPozoSoloSiNoHayMovimiento() {
        val g = game(listOf(t(0, 0)), listOf(t(3, 3)), board = listOf(t(3, 4)), pool = listOf(t(3, 5)))
        val r = GameEngine.draw(g, "p0").getOrThrow()
        assertEquals(2, r.players[0].hand.size)
        assertTrue(r.pool.isEmpty())
        val g2 = game(listOf(t(3, 3)), listOf(t(0, 0)), board = listOf(t(3, 4)), pool = listOf(t(3, 5)))
        assertTrue(GameEngine.draw(g2, "p0").isFailure)
    }

    @Test fun partidaBloqueadaTerminaTrasPasarTodos() {
        val g = game(listOf(t(0, 0)), listOf(t(1, 1)), board = listOf(t(3, 4)))
        val a = GameEngine.pass(g, "p0").getOrThrow()
        assertEquals(Status.PLAYING, a.status)
        val b = GameEngine.pass(a, "p1").getOrThrow()
        assertEquals(Status.FINISHED, b.status)
    }

    @Test fun quedarseSinFichasTerminaYDaBono() {
        val g = game(listOf(t(4, 5)), listOf(t(1, 1)), board = listOf(t(3, 4)))
        val r = GameEngine.play(g, "p0", Move(t(4, 5), Side.RIGHT)).getOrThrow()
        assertEquals(Status.FINISHED, r.status)
        assertEquals(9 + GameEngine.EMPTY_HAND_BONUS, r.players[0].score)
        assertEquals("p0", r.winnerUid)
    }

    @Test fun alcanzarMaximoDeRondasTermina() {
        val g = game(listOf(t(4, 5), t(0, 0)), listOf(t(5, 6), t(1, 1)), board = listOf(t(3, 4)))
            .copy(maxRounds = 1, turnCount = 1)
        assertEquals(Status.FINISHED, GameEngine.play(g, "p0", Move(t(4, 5), Side.RIGHT)).getOrThrow().status)
    }

    @Test fun empateSeDesempataPorMenosPoderesUsados() {
        val g = game(listOf(t(0, 1)), listOf(t(0, 2)))
        val s = g.copy(players = g.players.mapIndexed { i, p -> p.copy(score = 5, powersUsed = if (i == 0) 2 else 1) })
        assertEquals("p1", GameEngine.finish(s, "fin").winnerUid)
        val tie = s.copy(players = s.players.map { it.copy(powersUsed = 1) })
        assertNull(GameEngine.finish(tie, "fin").winnerUid)
    }

    @Test fun eventosModificanElEstado() {
        val g = game(listOf(t(1, 1)), listOf(t(2, 2)), pool = List(5) { t(0, it) })
        assertEquals(-1, GameEngine.applyEvent(g, EventType.REVERSE_DIRECTION).direction)
        assertEquals(3, GameEngine.applyEvent(g, EventType.POOL_SHRINK).pool.size)
        assertEquals(t(2, 2), GameEngine.applyEvent(g, EventType.ROTATE_HANDS).players[0].hand.first())
        assertEquals(EventType.DOUBLE_POINTS, GameEngine.applyEvent(g, EventType.DOUBLE_POINTS).activeEvent)
    }

    @Test fun puntosDoblesDuplicanLaJugada() {
        val g = game(listOf(t(4, 5), t(0, 0)), listOf(t(5, 6)), board = listOf(t(3, 4)))
            .copy(activeEvent = EventType.DOUBLE_POINTS, eventTurns = 3)
        assertEquals(18, GameEngine.play(g, "p0", Move(t(4, 5), Side.RIGHT)).getOrThrow().players[0].score)
    }

    @Test fun abandonarDaLaVictoriaAlRival() {
        val g = game(listOf(t(1, 1)), listOf(t(2, 2)))
        val r = GameEngine.abandon(g, "p0")
        assertEquals(Status.FINISHED, r.status)
        assertEquals("p1", r.winnerUid)
    }
}
