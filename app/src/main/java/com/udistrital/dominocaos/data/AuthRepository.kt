package com.udistrital.dominocaos.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {

    val user: Flow<LobbyPlayer?> = callbackFlow {
        val l = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.toPlayer()) }
        auth.addAuthStateListener(l)
        awaitClose { auth.removeAuthStateListener(l) }
    }

    fun current(): LobbyPlayer? = auth.currentUser?.toPlayer()

    suspend fun signIn(email: String, pass: String) {
        auth.signInWithEmailAndPassword(email.trim(), pass).await()
    }

    suspend fun signUp(name: String, email: String, pass: String) {
        val u = auth.createUserWithEmailAndPassword(email.trim(), pass).await().user
        u?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name.trim()).build())?.await()
        u?.reload()?.await()
    }

    fun signOut() = auth.signOut()

    private fun FirebaseUser.toPlayer() =
        LobbyPlayer(uid, displayName?.takeIf { it.isNotBlank() } ?: email.orEmpty().substringBefore("@"))
}
