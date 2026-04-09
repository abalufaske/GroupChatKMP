package org.alonsitos.chat

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize
import com.google.firebase.FirebasePlatform
import android.app.Application

class JvmFirebasePlatform : FirebasePlatform() {
    override fun store(key: String, value: String) {}
    override fun retrieve(key: String): String? = null
    override fun clear(key: String) {}
    override fun log(msg: String) = println("Firebase: $msg")
}

fun main() {
    // 1. Initialize the platform
    FirebasePlatform.initializeFirebasePlatform(JvmFirebasePlatform())

    // 2. Setup options from your Firebase Console
    val options = FirebaseOptions(
        apiKey = "AIzaSyCu2K-teq-m3t5863l9hflSHAgFqbrxjKg",
        applicationId = "1:458313722101:web:0302329a956763da923dbc",
        projectId = "family-chat-b16b3",
        databaseUrl = "https://family-chat-b16b3-default-rtdb.firebaseio.com",
        storageBucket = "family-chat-b16b3.firebasestorage.app",
        authDomain = "family-chat-b16b3.firebaseapp.com",
        gcmSenderId = "458313722101"
    )

    // 3. Initialize Firebase with a mock Application object to satisfy Firestore's lifecycle monitor
    try {
        val context = Application()
        Firebase.initialize(context, options, "[DEFAULT]")
        
        // Verification: ensure the app is registered
        val apps = com.google.firebase.FirebaseApp.getApps(context)
        println("Firebase initialized. Active apps: ${apps.map { it.name }}")
    } catch (e: Exception) {
        println("Firebase initialization failed: ${e.message}")
        e.printStackTrace()
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "AlonsitosGroupChat",
        ) {
            App()
        }
    }
}
