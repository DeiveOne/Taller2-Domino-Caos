package com.udistrital.dominocaos.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udistrital.dominocaos.data.AuthRepository
import com.udistrital.dominocaos.data.RoomRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.launch

class LobbyViewModel(
    private val auth: AuthRepository = AuthRepository(),
    private val rooms: RoomRepository = RoomRepository()
) : ViewModel() {
    var loading by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set

    fun create(rounds: Int, onDone: (String) -> Unit) {
        val user = auth.current()
        if (user == null) {
            error = "Usuario no autenticado. Inicia sesión de nuevo."
            return
        }
        launchSafe {
            val code = rooms.create(user, rounds)
            onDone(code)
        }
    }

    fun join(code: String, onDone: (String) -> Unit) {
        val user = auth.current()
        if (user == null) {
            error = "Usuario no autenticado. Inicia sesión de nuevo."
            return
        }
        launchSafe {
            rooms.join(code, user)
            onDone(code)
        }
    }

    private fun launchSafe(block: suspend () -> Unit) {
        error = null
        loading = true
        viewModelScope.launch {
            runCatching { block() }
                .onFailure {
                    error = parseError(it)
                }
            loading = false
        }
    }

    private fun parseError(e: Throwable): String {
        val msg = e.message.orEmpty()
        return when {
            e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                "Error de permisos en Firestore (PERMISSION_DENIED): Debes configurar las reglas de Firestore en Firebase Console para permitir lectura/escritura."
            e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.NOT_FOUND ->
                "Base de datos Firestore no encontrada (NOT_FOUND): Debes crear la base de datos Firestore en Firebase Console."
            msg.contains("PERMISSION_DENIED") ->
                "Permiso denegado en Firestore. Revisa las Reglas de Firestore en Firebase Console."
            msg.contains("NOT_FOUND") ->
                "La base de datos Firestore no existe. Ve a Firebase Console -> Firestore Database y haz clic en 'Crear base de datos'."
            else -> e.localizedMessage ?: "Ocurrió un error inesperado al conectar con Firestore."
        }
    }
}
