package com.udistrital.dominocaos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.udistrital.dominocaos.viewmodel.AuthViewModel

@Composable
fun AuthScreen(vm: AuthViewModel) {
    var register by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🁣 Dominó Caos", style = MaterialTheme.typography.headlineLarge)
        if (register) OutlinedTextField(name, { name = it }, label = { Text("Nombre") }, singleLine = true)
        OutlinedTextField(
            email, { email = it }, label = { Text("Correo") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        OutlinedTextField(
            pass, { pass = it }, label = { Text("Contraseña") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )
        vm.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button({ vm.submit(register, name, email, pass) }, enabled = !vm.loading) {
            Text(if (register) "Crear cuenta" else "Iniciar sesión")
        }
        TextButton({ register = !register }) {
            Text(if (register) "¿Ya tienes cuenta? Inicia sesión" else "¿No tienes cuenta? Regístrate")
        }
    }
}
