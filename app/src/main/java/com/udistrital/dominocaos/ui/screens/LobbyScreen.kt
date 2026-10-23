package com.udistrital.dominocaos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.udistrital.dominocaos.viewmodel.LobbyViewModel

@Composable
fun LobbyScreen(onRoom: (String) -> Unit, onSignOut: () -> Unit, vm: LobbyViewModel = viewModel()) {
    var code by remember { mutableStateOf("") }
    var rounds by remember { mutableFloatStateOf(10f) }

    val background = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF120320),
            Color(0xFF2E094E),
            Color(0xFF5E14A7)
        )
    )

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = Color(0xFF00E5FF), // Acento Cyan para unirse
        unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
        focusedLabelColor = Color(0xFF00E5FF),
        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
        cursorColor = Color(0xFF00E5FF)
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "SALAS DE JUEGO",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )

            // TARJETA 1: CREAR SALA
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(20.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("NUEVA PARTIDA", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Rondas máximas: ${rounds.toInt()}", color = Color.White.copy(alpha = 0.8f))
                    Slider(
                        value = rounds,
                        onValueChange = { rounds = it },
                        valueRange = 4f..20f,
                        enabled = !vm.loading,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFE040FB),
                            activeTrackColor = Color(0xFFE040FB)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { vm.create(rounds.toInt(), onRoom) },
                        enabled = !vm.loading,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE040FB)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("CREAR SALA", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Divisor visual
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.2f))
                Text(" O ", color = Color.White.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 8.dp))
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.2f))
            }

            // TARJETA 2: UNIRSE A SALA
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(20.dp),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("UNIRSE A PARTIDA", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.uppercase().take(5) },
                        label = { Text("Código de sala (5 letras)") },
                        singleLine = true,
                        enabled = !vm.loading,
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { vm.join(code, onRoom) },
                        enabled = code.length == 5 && !vm.loading,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E5FF), // Cyan para diferenciar
                            contentColor = Color.Black // Texto negro sobre cyan resalta bien
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        // Ícono cambiado a ArrowForward
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("ENTRAR", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Mostrar Errores
            vm.error?.let {
                Text(text = it, color = Color(0xFFFF5252), textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón de salir al fondo
            TextButton(onClick = onSignOut, enabled = !vm.loading) {
                Text("Cerrar sesión", color = Color.White.copy(alpha = 0.6f))
            }
        }
    }
}