package com.udistrital.dominocaos.ui.i18n

object AppStrings {
    private val isEs get() = LanguageManager.currentLanguage == AppLanguage.SPANISH

    // General & Auth
    val appTitle get() = if (isEs) "🁣 Dominó Caos" else "🁣 Domino Chaos"
    val name get() = if (isEs) "Nombre" else "Name"
    val email get() = if (isEs) "Correo" else "Email"
    val password get() = if (isEs) "Contraseña" else "Password"
    val signIn get() = if (isEs) "Iniciar sesión" else "Sign in"
    val signUp get() = if (isEs) "Crear cuenta" else "Create account"
    val signOut get() = if (isEs) "Cerrar sesión" else "Sign out"
    val hasAccount get() = if (isEs) "¿Ya tienes cuenta? Inicia sesión" else "Already have an account? Sign in"
    val noAccount get() = if (isEs) "¿No tienes cuenta? Regístrate" else "Don't have an account? Register"

    // Lobby
    val gameRooms get() = if (isEs) "SALAS DE JUEGO" else "GAME ROOMS"
    val newMatch get() = if (isEs) "NUEVA PARTIDA" else "NEW MATCH"
    val maxRounds get() = if (isEs) "Rondas máximas" else "Max rounds"
    val createRoom get() = if (isEs) "CREAR SALA" else "CREATE ROOM"
    val joinMatch get() = if (isEs) "UNIRSE A PARTIDA" else "JOIN MATCH"
    val roomCodeHint get() = if (isEs) "Código de sala (5 letras)" else "Room code (5 letters)"
    val enter get() = if (isEs) "ENTRAR" else "ENTER"
    val wins get() = if (isEs) "Victorias" else "Wins"
    val points get() = if (isEs) "pts" else "pts"

    // Settings
    val settings get() = if (isEs) "Ajustes de la App" else "App Settings"
    val language get() = if (isEs) "Idioma" else "Language"
    val close get() = if (isEs) "Cerrar" else "Close"

    // Room & Game
    val waitingRoom get() = if (isEs) "SALA DE ESPERA" else "WAITING ROOM"
    val inviteCode get() = if (isEs) "CÓDIGO DE INVITACIÓN" else "INVITATION CODE"
    val roundsCount get(): (Int) -> String = { r -> if (isEs) "Partida a $r rondas" else "Game of $r rounds" }
    val playersCount get(): (Int) -> String = { c -> if (isEs) "JUGADORES ($c/4)" else "PLAYERS ($c/4)" }
    val host get() = if (isEs) "Anfitrión" else "Host"
    val startGame get() = if (isEs) "INICIAR PARTIDA" else "START GAME"
    val waitingForHost get() = if (isEs) "Esperando al anfitrión..." else "Waiting for host..."
    val leaveRoom get() = if (isEs) "SALIR DE LA SALA" else "LEAVE ROOM"
    val yourTurn get(): (Int) -> String = { s -> if (isEs) "Tu turno ($s s)" else "Your turn (${s}s)" }
    val turnOf get(): (String, Int) -> String = { name, s -> if (isEs) "Turno de $name ($s s)" else "$name's turn (${s}s)" }
    val wonGame get(): (String) -> String = { name -> if (isEs) "🏆 Ganó $name" else "🏆 $name won" }
    val tieGame get() = if (isEs) "🤝 Empate" else "🤝 Tie Game"
    val activeEvent get(): (String, Int) -> String = { label, t -> if (isEs) "⚡ Evento activo: $label ($t)" else "⚡ Active event: $label ($t)" }
    val drawTile get() = if (isEs) "Robar" else "Draw"
    val passTurn get() = if (isEs) "Pasar" else "Pass"
    val leaveGame get() = if (isEs) "Abandonar partida" else "Leave game"
    val rematch get() = if (isEs) "🔄 Revancha (Nueva partida)" else "🔄 Rematch (New match)"
}
