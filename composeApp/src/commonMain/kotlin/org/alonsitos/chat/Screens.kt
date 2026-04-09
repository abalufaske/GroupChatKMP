package org.alonsitos.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import alonsitosgroupchat.composeapp.generated.resources.Res
import alonsitosgroupchat.composeapp.generated.resources.ic_family

sealed class Screen {
    object Login : Screen()
    data class ProfileSelection(val familyKey: String) : Screen()
    data class ChatSelection(val myProfile: String) : Screen()
    data class Chat(val myProfile: String, val otherProfile: String) : Screen()
}

@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_family),
            contentDescription = "Family Icon",
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Alonsitos Group Chat", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))
        Text("Enter the Family Key", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; error = null },
            label = { Text("Family Key") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = error != null,
            enabled = !isLoading
        )
        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    isLoading = true
                    scope.launch {
                        if (verifyAndSaveUser(password)) {
                            onLoginSuccess(password)
                        } else {
                            error = "Invalid Family Key"
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = password.isNotBlank()
            ) {
                Text("Verify Key")
            }
        }
    }
}

@Composable
fun ProfileSelectionScreen(familyKey: String, onProfileSelected: (String) -> Unit) {
    val profiles = ALL_PROFILES
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text("Who are you?", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(profiles) { profile ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isLoading = true
                                scope.launch {
                                    if (verifyAndSaveUser(familyKey, profile)) {
                                        onProfileSelected(profile)
                                    } else {
                                        isLoading = false
                                    }
                                }
                            }
                    ) {
                        Text(
                            text = profile,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatSelectionScreen(myProfile: String, onLogout: () -> Unit, onContactSelected: (String) -> Unit) {
    val profiles = ALL_PROFILES.filter { it != myProfile }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp, top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Hello $myProfile!", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onLogout) {
                Text("Logout")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Select someone to chat with:", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(profiles) { profile ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onContactSelected(profile) }
                ) {
                    Text(
                        text = profile,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun ChatScreen(myProfile: String, otherProfile: String, onBack: () -> Unit) {
    var messageText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<Message>() }
    val scope = rememberCoroutineScope()
    val chatId = getChatId(myProfile, otherProfile)
    val listState = rememberLazyListState()

    // Observe messages from Firestore
    LaunchedEffect(chatId) {
        val db = Firebase.firestore
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Direction.ASCENDING)
            .snapshots
            .collectLatest { snapshot ->
                val newMessages = snapshot.documents.map { doc ->
                    doc.data<Message>()
                }
                messages.clear()
                messages.addAll(newMessages)
                // Scroll to bottom when new messages arrive
                if (messages.isNotEmpty()) {
                    listState.animateScrollToItem(messages.size - 1)
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Bar
        Surface(shadowElevation = 4.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp,top = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Text("<") // Simple back icon replacement
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(otherProfile, style = MaterialTheme.typography.headlineSmall)
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages) { msg ->
                MessageBubble(msg, isMine = msg.sender == myProfile)
            }
        }

        // Input Area
        Surface(shadowElevation = 8.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            val textToSend = messageText
                            messageText = ""
                            scope.launch {
                                sendMessage(chatId, myProfile, textToSend)
                            }
                        }
                    },
                    enabled = messageText.isNotBlank()
                ) {
                    Text("Send")
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, isMine: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isMine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Text(
            text = message.sender,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
        )
    }
}

fun getChatId(user1: String, user2: String): String {
    return if (user1 < user2) "${user1}_${user2}" else "${user2}_${user1}"
}

suspend fun sendMessage(chatId: String, sender: String, text: String) {
    try {
        val db = Firebase.firestore
        val message = Message(
            sender = sender,
            text = text,
            timestamp = Timestamp.now()
        )
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(message)
    } catch (e: Exception) {
        println("Error sending message: ${e.message}")
    }
}

suspend fun verifyAndSaveUser(enteredKey: String, selectedName: String? = null): Boolean {
    val db = Firebase.firestore

    try {
        // 1. Fetch the secret key from your Firestore "settings" document
        val document = db.collection("family_config").document("settings").get()
        val serverKey = document.get<String>("access_code")

        // 2. Compare the key the user typed with the one in the cloud
        if (serverKey == enteredKey) {
            // SUCCESS: Save their name locally so the app "remembers" them
            // KVault is like a shared "cookie" for all platforms
            if (selectedName != null) {
                getKVault().set("my_profile", selectedName)
            }
            return true
        }
    } catch (e: Exception) {
        println("Error connecting to Firebase: ${e.message}")
    }
    return false
}
