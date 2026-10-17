package com.udistrital.dominocaos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.udistrital.dominocaos.ui.screens.AuthScreen
import com.udistrital.dominocaos.ui.screens.LobbyScreen
import com.udistrital.dominocaos.ui.screens.RoomScreen
import com.udistrital.dominocaos.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Surface(Modifier.fillMaxSize().safeDrawingPadding()) { AppNav() } } }
    }
}

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val authVm: AuthViewModel = viewModel()
    val user by authVm.user.collectAsState()

    NavHost(nav, startDestination = if (user == null) "auth" else "lobby") {
        composable("auth") { AuthScreen(authVm) }
        composable("lobby") { LobbyScreen(onRoom = { nav.navigate("room/$it") }, onSignOut = authVm::signOut) }
        composable("room/{code}") { entry ->
            RoomScreen(entry.arguments?.getString("code").orEmpty(), onExit = { nav.popBackStack() })
        }
    }
    // Redirige cuando cambia la sesión (login / logout)
    LaunchedEffect(user?.uid) {
        nav.navigate(if (user == null) "auth" else "lobby") { popUpTo(0); launchSingleTop = true }
    }
}
