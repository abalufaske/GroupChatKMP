package org.alonsitos.chat

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun App() {
    val kvault = remember { getKVault() }
    val savedProfile = remember { kvault.get("my_profile") }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            var currentScreen by remember {
                mutableStateOf<Screen>(
                    if (savedProfile != null) Screen.ChatSelection(savedProfile)
                    else Screen.Login
                )
            }

            when (val screen = currentScreen) {
                is Screen.Login -> {
                    LoginScreen(
                        onLoginSuccess = { familyKey ->
                            currentScreen = Screen.ProfileSelection(familyKey)
                        }
                    )
                }
                is Screen.ProfileSelection -> {
                    ProfileSelectionScreen(
                        familyKey = screen.familyKey,
                        onProfileSelected = { profile ->
                            currentScreen = Screen.ChatSelection(profile)
                        }
                    )
                }
                is Screen.ChatSelection -> {
                    ChatSelectionScreen(
                        myProfile = screen.myProfile,
                        onLogout = {
                            kvault.clear()
                            currentScreen = Screen.Login
                        },
                        onContactSelected = { otherProfile ->
                            currentScreen = Screen.Chat(screen.myProfile, otherProfile)
                        }
                    )
                }
                is Screen.Chat -> {
                    ChatScreen(
                        myProfile = screen.myProfile,
                        otherProfile = screen.otherProfile,
                        onBack = {
                            currentScreen = Screen.ChatSelection(screen.myProfile)
                        }
                    )
                }
            }
        }
    }
}