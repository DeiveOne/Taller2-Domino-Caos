package com.udistrital.dominocaos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.udistrital.dominocaos.viewmodel.AuthViewModel

@Composable
fun AuthScreen(vm: AuthViewModel) {
    var register by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    // Degradado de morado oscuro a morado vibrante
    val background = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF120320), // Morado casi negro
            Color(0xFF2E094E), // Morado oscuro
            Color(0xFF5E14A7)  // Morado vibrante
        )
    )

    // Colores para los campos de texto en fondo oscuro
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = Color(0xFFE040FB),
        unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
        focusedLabelColor = Color(0xFFE040FB),
        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
        cursorColor = Color(0xFFE040FB)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Título principal e Ícono cambiado a Star
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFE040FB),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "DOMINÓ CAOS",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )
            Text(
                text = if (register) "Crea tu cuenta para jugar" else "Bienvenido de vuelta",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Tarjeta tipo "Cristal" para el formulario
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (register) {
                        OutlinedTextField(
                            value = name, onValueChange = { name = it },
                            label = { Text("Nombre de usuario") },
                            singleLine = true,
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = email, onValueChange = { email = it },
                        label = { Text("Correo electrónico") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = pass, onValueChange = { pass = it },
                        label = { Text("Contraseña") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    vm.error?.let {
                        Text(it, color = Color(0xFFFF5252), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botón principal
                    Button(
                        onClick = { vm.submit(register, name, email, pass) },
                        enabled = !vm.loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE040FB), // Acento magenta/morado claro
                            disabledContainerColor = Color(0xFFE040FB).copy(alpha = 0.5f)
                        )
                    ) {
                        if (vm.loading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = if (register) "CREAR CUENTA" else "JUGAR",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de texto para alternar modo
            TextButton(onClick = { register = !register }) {
                Text(
                    text = if (register) "¿Ya tienes cuenta? Inicia sesión" else "¿No tienes cuenta? Regístrate",
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}