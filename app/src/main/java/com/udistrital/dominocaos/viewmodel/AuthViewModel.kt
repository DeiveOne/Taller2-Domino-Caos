package com.udistrital.dominocaos.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.udistrital.dominocaos.data.AuthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(private val repo: AuthRepository = AuthRepository()) : ViewModel() {
    val user = repo.user.stateIn(viewModelScope, SharingStarted.Eagerly, repo.current())
    var loading by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set

    fun submit(register: Boolean, name: String, email: String, pass: String) {
        if (email.isBlank() || pass.length < 6 || (register && name.isBlank())) {
            error = "Completa los campos (contraseña de mínimo 6 caracteres)"; return
        }
        viewModelScope.launch {
            loading = true; error = null
            runCatching { if (register) repo.signUp(name, email, pass) else repo.signIn(email, pass) }
                .onFailure {
                    val msg = it.message.orEmpty()
                    error = when {
                        msg.contains("CONFIGURATION_NOT_FOUND") ->
                            "Error de Firebase: Debes habilitar 'Correo electrónico/Contraseña' en Firebase Console > Autenticación."
                        else -> it.localizedMessage ?: "Ocurrió un error al autenticar"
                    }
                }
            loading = false
        }
    }

    fun signOut() = repo.signOut()
}
