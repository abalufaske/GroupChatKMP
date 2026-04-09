package org.alonsitos.chat

import kotlinx.serialization.Serializable
import dev.gitlive.firebase.firestore.Timestamp

@Serializable
data class Message(
    val sender: String = "",
    val text: String = "",
    val timestamp: Timestamp = Timestamp.now()
)

@Serializable
data class FamilySettings(
    val access_code: String,
    val members: List<String>
)

val ALL_PROFILES = listOf("Abuelos", "Primos", "Papa", "Annefleur")

