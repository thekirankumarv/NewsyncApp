package com.thekirankumarv.newsync.chat.presentation

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.*
import com.thekirankumarv.newsync.R
import com.thekirankumarv.newsync.navigation.Dest
import androidx.compose.ui.platform.LocalContext

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ChatListScreen(
    viewModel: ChatViewModel,
    onNavigateToChat: (Dest.ChatScreen) -> Unit,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val users = uiState.users
    val messages = uiState.messages

    var showAddUserDialog by remember { mutableStateOf(false) }
    var searchEmail by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    var userNotFoundMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userNotFoundMessage) {
        userNotFoundMessage?.let {
            snackbarHostState.showSnackbar(it)
            userNotFoundMessage = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    HapticFeedback.performHapticFeedback(context, HapticFeedback.FeedbackType.CLICK)
                    showAddUserDialog = true 
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.Add, "Add User")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(top = 8.dp)
        ) {
            if (users.isEmpty()) {
                LottieNoUsersAnimation()
                Text("No users found. Add users to start chatting.", modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(users.filter { it.name?.isNotBlank() == true }) { user -> // Use `name` instead of `displayName`
                        val userMessages = messages.filter { it.receiverId == user.userId || it.senderId == user.userId }
                        ChatUserItem(
                            user = user,
                            onClick = { onNavigateToChat(Dest.ChatScreen(user.userId)) },
                            onDelete = { 
                                HapticFeedback.performHapticFeedback(context)
                                viewModel.deleteChat(user) 
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddUserDialog) {
        AlertDialog(
            onDismissRequest = { showAddUserDialog = false },
            title = { Text("Add User") },
            text = {
                OutlinedTextField(
                    value = searchEmail,
                    onValueChange = { searchEmail = it },
                    label = { Text("Enter Email") },
                    minLines = 1
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.searchUserByEmail(searchEmail) { isUserFound, result ->
                        when {
                            result == "already_exists" ->
                                userNotFoundMessage = "User already in chat list"
                            isUserFound && result != null ->
                                viewModel.addUserToChat(result)
                            else ->
                                userNotFoundMessage = "User not found!"
                        }
                    }
                    showAddUserDialog = false
                    searchEmail = "" // Clear the input
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddUserDialog = false
                    searchEmail = "" // Clear the input
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun LottieNoUsersAnimation() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.nousers))
    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )
}

@Composable
fun ChatUserItem(
    user: ChatUser,
    onClick: () -> Unit,
    onDelete: (ChatUser) -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.name ?: "Unknown User", // Use `name` instead of `displayName`
                    style = MaterialTheme.typography.titleMedium
                )
                user.lastMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(
                onClick = { onDelete(user) }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete User"
                )
            }
        }
    }
}