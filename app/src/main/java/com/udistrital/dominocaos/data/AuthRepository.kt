package com.udistrital.dominocaos.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

data class UserStats(
    val uid: String = "",
    val name: String = "",
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val totalScore: Int = 0
)

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    val user: Flow<LobbyPlayer?> = callbackFlow {
        val l = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.toPlayer()) }
        auth.addAuthStateListener(l)
        awaitClose { auth.removeAuthStateListener(l) }
    }

    fun current(): LobbyPlayer? = auth.currentUser?.toPlayer()

    fun observeStats(uid: String): Flow<UserStats?> {
        if (uid.isBlank()) return flowOf(null)
        return callbackFlow {
            val reg = db.collection("users").document(uid).addSnapshotListener { snap, err ->
                if (err != null) {
                    trySend(null)
                } else {
                    val stats = snap?.takeIf { it.exists() }?.let {
                        UserStats(
                            uid = it.id,
                            name = it.getString("name").orEmpty(),
                            gamesPlayed = (it.getLong("gamesPlayed") ?: 0L).toInt(),
                            gamesWon = (it.getLong("gamesWon") ?: 0L).toInt(),
                            totalScore = (it.getLong("totalScore") ?: 0L).toInt()
                        )
                    }
                    trySend(stats)
                }
            }
            awaitClose { reg.remove() }
        }
    }

    fun observeLeaderboard(limit: Int = 10): Flow<List<UserStats>> = callbackFlow {
        val reg = db.collection("users")
            .orderBy("gamesWon", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) {
                    trySend(emptyList())
                } else {
                    val list = snap.documents.map { doc ->
                        UserStats(
                            uid = doc.id,
                            name = doc.getString("name").orEmpty(),
                            gamesPlayed = (doc.getLong("gamesPlayed") ?: 0L).toInt(),
                            gamesWon = (doc.getLong("gamesWon") ?: 0L).toInt(),
                            totalScore = (doc.getLong("totalScore") ?: 0L).toInt()
                        )
                    }
                    trySend(list)
                }
            }
        awaitClose { reg.remove() }
    }

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
